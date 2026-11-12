import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { deflateSync } from 'node:zlib';

const base = process.env.RESTMAN_BASE_URL || 'http://localhost:18085/pttk';
const runName = '__VERIFY_RESTMAN_' + Date.now();
let checks = 0;
function check(condition, message) { assert.ok(condition, message); console.log('PASS:', message); checks++; }
function hidden(html, name) {
    const value = html.match(new RegExp(`name="${name}"[^>]*value="([^"]*)"`));
    assert.ok(value, `Missing hidden field ${name}`); return value[1];
}
class Client {
    cookie = '';
    async request(route, body, multipart = false) {
        let url = route.startsWith('http') ? route : base + route;
        let options = { redirect: 'manual', headers: { Cookie: this.cookie } };
        if (body) { options.method = 'POST'; options.body = multipart ? body : new URLSearchParams(body); }
        for (let redirects = 0; redirects < 8; redirects++) {
            const response = await fetch(url, options);
            const cookie = response.headers.getSetCookie().find(value => value.startsWith('JSESSIONID='));
            if (cookie) this.cookie = cookie.split(';')[0];
            if ([301,302,303].includes(response.status)) {
                url = new URL(response.headers.get('location'),url).href;
                options = { redirect: 'manual', headers: { Cookie: this.cookie } }; continue;
            }
            // Khoảng trắng giữa các thẻ không ảnh hưởng nội dung bảng cần kiểm tra.
            const html = (await response.text()).replace(/>\s+</g, '><');
            return { status: response.status, html, url };
        }
        throw new Error('Too many redirects');
    }
    async login(userName, password) {
        const page = await this.request('/LoginServlet5');
        return this.request('/LoginServlet5', {csrf: hidden(page.html,'csrf'),userName,password});
    }
}
function imagePng() {
    const chunk = (name,data) => {
        const payload = Buffer.concat([Buffer.from(name),data]); let crc = 0xffffffff;
        for (const byte of payload) { crc ^= byte; for(let i=0;i<8;i++) crc=(crc>>>1)^((crc&1)?0xedb88320:0); }
        const head=Buffer.alloc(4),tail=Buffer.alloc(4); head.writeUInt32BE(data.length); tail.writeUInt32BE((crc^0xffffffff)>>>0);
        return Buffer.concat([head,payload,tail]);
    };
    const header=Buffer.alloc(13); header.writeUInt32BE(1,0);header.writeUInt32BE(1,4);header[8]=8;header[9]=2;
    return Buffer.concat([Buffer.from('89504e470d0a1a0a','hex'),chunk('IHDR',header),chunk('IDAT',deflateSync(Buffer.from([0,120,120,120]))),chunk('IEND',Buffer.alloc(0))]);
}
const anonymous = new Client();
check((await anonymous.request('/ComboManagementServlet5')).url.endsWith('/LoginServlet5'),'Unauthenticated manager route requires login');
check((await anonymous.login('quanly','wrong')).html.includes('không đúng'),'Wrong password rejected');
const customer=new Client();
let page=await customer.login('khachhang','Khach@123');
check(page.status===200 && page.html.includes('Trang chủ khách hàng'),'Customer login and homepage');
check((await customer.request('/ComboManagementServlet5')).status===403,'Customer cannot access manager route');
check((await customer.request('/DishSearchServlet5?keyword=')).html.includes('Vui lòng nhập tên'),'Empty keyword validation');
page=await customer.request('/DishSearchServlet5?keyword='+encodeURIComponent('Phở'));
check(page.status===200 && page.html.includes('Phở bò') && page.html.includes('Phở gà') && page.html.includes('Phở đặc biệt'),'Vietnamese search returns the three documented dishes');
page=await customer.request('/DishDetailServlet5?id=1&keyword='+encodeURIComponent('Phở'));
check(page.status===200 && ['45.000đ','55.000đ','65.000đ','Đang phục vụ'].every(value=>page.html.includes(value)),'Dish detail has all sizes, prices and status');
check(page.html.includes('keyword=Ph%E1%BB%9F'),'Detail back link retains search keyword');
check((await customer.request('/DishDetailServlet5?id=999999')).status===404,'Missing dish handled');
check((await customer.request('/DishDetailServlet5?id=bad')).status===400,'Invalid dish ID handled');
check((await customer.request('/DishSearchServlet5?keyword=%25')).html.includes('Không tìm thấy'),'LIKE wildcard is treated as literal input');
check((await customer.request('/DishSearchServlet5?keyword='+encodeURIComponent("' OR 1=1 --"))).html.includes('Không tìm thấy'),'Search uses parameterized SQL');
page=await customer.request('/DishSearchServlet5?keyword='+encodeURIComponent('<script>alert(1)</script>'));
check(!page.html.includes('<script>alert(1)</script>') && page.html.includes('&lt;script&gt;'),'Search output escapes HTML');

const manager=new Client();
page=await manager.login('quanly','Quanly@123');
check(page.status===200 && page.html.includes('Trang chủ nhân viên quản lý'),'Manager login and homepage');
check((await manager.request('/DishSearchUI5.jsp')).status===403,'Manager and customer modules enforce roles');
page=await manager.request('/ComboManagementServlet5');
check(page.html.includes('Combo Cơm Trưa') && page.html.includes('Combo Gia Đình'),'Existing combo list from MySQL');
page=await manager.request('/ComboCreateServlet5?action=new');
let csrf=hidden(page.html,'csrf'),draftToken=hidden(page.html,'draftToken');
const fields={name:runName,price:'129000',description:'Combo gồm 2 Phở bò size Vừa và 1 Phở gà size Nhỏ.',startDate:'2026-12-01',endDate:'2026-12-31',csrf,draftToken,action:'information'};
const information = async (overrides={},image) => {
    const form=new FormData();for(const [key,value] of Object.entries({...fields,...overrides})) form.set(key,value);
    if(image) form.set('image',new Blob([image.bytes],{type:image.type}),image.name);
    return manager.request('/ComboCreateServlet5',form,true);
};
page=await information({price:'0'});
check(page.html.includes('Giá bán phải lớn hơn') && page.html.includes(runName),'Invalid price rejected and form values retained');
check((await information({endDate:'2026-11-01'})).html.includes('Ngày kết thúc phải'),'Reversed combo dates rejected');
page=await information({}, {bytes:Buffer.from('<script>bad</script>'),type:'image/png',name:'fake.png'});
check(page.html.includes('ảnh PNG, JPG hoặc GIF hợp lệ'),'Spoofed image rejected');
page=await information({}, {bytes:imagePng(),type:'image/png',name:'ComboPho3Suat.png'});
check(page.status===200 && page.html.includes('Chọn món cho combo') && page.html.includes('ComboPho3Suat.png'),'Valid metadata and image preserved in session');
const post=values=>manager.request('/ComboCreateServlet5',{csrf,draftToken,...values});
check((await post({action:'confirm'})).html.includes('ít nhất một món'),'Cannot confirm empty combo');
page=await manager.request('/ComboCreateServlet5?dishId=1&keyword='+encodeURIComponent('Phở bò'));
check(page.status===200 && page.html.includes('role="dialog"') && ['Nhỏ','Vừa','Lớn'].every(v=>page.html.includes(v)),'Size popup renders over selection page');
check((await post({action:'add',detailId:'2',dishId:'1',quantity:'0'})).html.includes('không hợp lệ'),'Zero quantity rejected');
check((await post({action:'add',detailId:'2',dishId:'2',quantity:'1'})).html.includes('không còn phục vụ'),'Mismatched dish and size rejected');
check((await post({action:'add',detailId:'11',dishId:'6',quantity:'1'})).html.includes('không còn phục vụ'),'Unavailable dish rejected');
await post({action:'add',detailId:'1',dishId:'1',quantity:'1'});
page=await post({action:'add',detailId:'1',dishId:'1',quantity:'1'});
check(page.html.includes('<td>Nhỏ</td><td>2</td>'),'Same dish and size merges quantities');
page=await post({action:'remove',detailId:'1'});
check(page.html.includes('Chưa chọn món ăn nào'),'Selected row can be removed');
await post({action:'add',detailId:'2',dishId:'1',quantity:'2'});
await post({action:'add',detailId:'4',dishId:'2',quantity:'1'});
page=await manager.request('/ComboCreateServlet5?categoryId=3');
check(page.html.includes('Nước chanh') && !page.html.includes('Phở đặc biệt') && page.html.includes('<td>Vừa</td><td>2</td>'),'Category filtering preserves selected dishes');
page=await manager.request('/ComboCreateServlet5?keyword='+encodeURIComponent('Phở bò')+'&categoryId=1');
check(page.html.includes('1 kết quả') && !page.html.includes('Phở đặc biệt'),'Name and category filters combine');
page=await manager.request('/ComboCreateServlet5?action=information');
check(page.html.includes(runName) && page.html.includes('ComboPho3Suat.png'),'Back to metadata retains form and image');
page=await information({description:'Đã sửa mô tả'});
check(page.html.includes('Đã sửa mô tả') && page.html.includes('<td>Vừa</td><td>2</td>') && page.html.includes('<td>Nhỏ</td><td>1</td>'),'Editing metadata retains selected sizes and quantities');
check((await manager.request('/ComboCreateServlet5',{action:'confirm',csrf:'bad',draftToken})).status===400,'Mutating requests validate CSRF token');
await Promise.all([post({action:'confirm'}),post({action:'confirm'})]);
page=await manager.request('/ComboManagementServlet5');
check(page.status===200 && page.html.split(runName).length===2,'Concurrent double confirmation saves exactly one combo');
const props=readFileSync('nbproject/project.properties','utf8');
const connector=props.match(/^file\.reference\.mysql[^=]*=(.*)$/m)[1].trim().replaceAll('\\\\','\\');
const localProps=readFileSync('nbproject/private/private.properties','utf8');
const tomcat=process.env.CATALINA_HOME || localProps.match(/^j2ee\.server\.home=(.*)$/m)[1].trim();
const cp=[path.resolve('build/web/WEB-INF/classes'),connector,path.join(tomcat,'lib','*')].join(path.delimiter);
console.log(execFileSync('java',['-cp',cp,'test/integration/DatabaseIntegration5.java',runName],{encoding:'utf8'}).trim());
page=await manager.request('/ComboCreateServlet5?action=new');
draftToken=hidden(page.html,'draftToken');
fields.draftToken=draftToken;
await information({}, {bytes:imagePng(),type:'image/png',name:'cancel.png'});
page=await manager.request('/ComboCreateServlet5?action=information');
const draftImage=page.html.match(/src="([^"]*ImageServlet5[^"]*)"/)[1].replaceAll('&amp;','&');
check((await manager.request(new URL(draftImage,base).href)).status===200,'Uploaded draft image served');
await post({action:'cancel'});
check((await manager.request(new URL(draftImage,base).href)).status===404,'Cancelling deletes draft image');
page=await manager.request('/ManagerMainUI5.jsp');
await manager.request('/LogoutServlet5',{csrf:hidden(page.html,'csrf')});
check((await manager.request('/ComboManagementServlet5')).url.endsWith('/LoginServlet5'),'Logout invalidates session');
console.log(`Completed ${checks} HTTP checks plus database transaction checks. Temporary test combo and image cleaned.`);
