import assert from 'node:assert/strict';
import { mkdirSync, writeFileSync } from 'node:fs';

// Dùng Edge/Chrome headless đã mở với --remote-debugging-port=19225; không cần thư viện npm.
const base=process.env.RESTMAN_BASE_URL || 'http://localhost:18085/pttk';
const endpoint=process.env.RESTMAN_CDP_URL || 'http://localhost:19225';
const target=await (await fetch(endpoint+'/json/new?about:blank',{method:'PUT'})).json();
const ws=new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve,reject)=>{ws.onopen=resolve;ws.onerror=reject;});
let id=0;const waiting=new Map(),events=new Map(),errors=[];
ws.onmessage=event=>{
    const message=JSON.parse(event.data);
    if(message.id){const task=waiting.get(message.id);waiting.delete(message.id);message.error?task.reject(new Error(JSON.stringify(message.error))):task.resolve(message.result);}
    else {if(message.method==='Runtime.exceptionThrown') errors.push(message.params);for(const handler of events.get(message.method)||[]) handler(message.params);}
};
function call(method,params={}) {return new Promise((resolve,reject)=>{const key=++id;waiting.set(key,{resolve,reject});ws.send(JSON.stringify({id:key,method,params}));});}
async function evaluate(expression){const result=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(result.exceptionDetails)throw new Error(JSON.stringify(result.exceptionDetails));return result.result.value;}
async function load(action){
    let resolveLoad;const loaded=new Promise(resolve=>{resolveLoad=resolve;});events.set('Page.loadEventFired',[resolveLoad]);
    await action();
    let timeout;try{await Promise.race([loaded,new Promise((_,reject)=>{timeout=setTimeout(()=>reject(new Error('Navigation timed out')),15000);})]);}finally{clearTimeout(timeout);events.delete('Page.loadEventFired');}
}
const go=route=>load(()=>call('Page.navigate',{url:base+route}));
const submit=expression=>load(()=>evaluate(expression));
const output='build/verification';mkdirSync(output,{recursive:true});
async function screenshot(name){await evaluate('document.fonts.ready.then(()=>true)');const shot=await call('Page.captureScreenshot',{format:'png',captureBeyondViewport:true});writeFileSync(`${output}/${name}.png`,Buffer.from(shot.data,'base64'));}
try {
    await call('Page.enable');await call('Runtime.enable');
    await call('Emulation.setDeviceMetricsOverride',{width:1280,height:900,deviceScaleFactor:1,mobile:false});
    await go('/LoginServlet5');
    await submit(`document.querySelector('#userName').value='quanly';document.querySelector('#password').value='Quanly@123';document.querySelector('.stack-form').requestSubmit()`);
    assert.ok((await evaluate('document.body.innerText')).includes('Trang chủ nhân viên quản lý'));
    assert.equal(await evaluate(`document.querySelector('form[action$="LogoutServlet5"] button').textContent`),'Đăng xuất');
    await screenshot('manager-home');
    await go('/ComboManagementServlet5');await screenshot('combo-list');
    await go('/ComboCreateServlet5?action=new');
    await evaluate(`document.querySelector('#name').value='Combo Phở 3 suất';document.querySelector('#price').value='129000';document.querySelector('#description').value='Combo gồm 2 Phở bò size Vừa và 1 Phở gà size Nhỏ.';document.querySelector('#startDate').value='2026-12-01';document.querySelector('#endDate').value='2026-12-31'`);
    await screenshot('combo-information');
    await submit(`document.querySelector('#combo-information').requestSubmit()`);
    await screenshot('combo-selection');
    await go('/ComboCreateServlet5?dishId=1');
    assert.equal(await evaluate(`document.activeElement.id`),'size-1');
    assert.equal(await evaluate(`document.querySelector('h1').inert`),true);
    await screenshot('combo-size-popup');
    await call('Emulation.setDeviceMetricsOverride',{width:390,height:844,deviceScaleFactor:1,mobile:true});
    assert.ok(await evaluate('document.documentElement.scrollWidth <= innerWidth'));
    await screenshot('combo-size-popup-mobile');
    await go('/ComboCreateServlet5?action=information');
    await submit(`document.querySelector('button[name=action][value=cancel]').form.requestSubmit(document.querySelector('button[name=action][value=cancel]'))`);
    await submit(`document.querySelector('form[action$="LogoutServlet5"]').requestSubmit()`);
    await submit(`document.querySelector('#userName').value='khachhang';document.querySelector('#password').value='Khach@123';document.querySelector('.stack-form').requestSubmit()`);
    await go('/DishSearchUI5.jsp');await screenshot('dish-search-mobile');
    await call('Emulation.setDeviceMetricsOverride',{width:1280,height:900,deviceScaleFactor:1,mobile:false});
    await go('/DishSearchServlet5?keyword='+encodeURIComponent('Phở'));await screenshot('dish-search-results');
    await go('/DishDetailServlet5?id=1&keyword='+encodeURIComponent('Phở'));await screenshot('dish-detail');
    assert.ok((await evaluate('document.body.innerText')).includes('55.000đ'));
    await submit(`document.querySelector('form[action$="LogoutServlet5"]').requestSubmit()`);
    assert.equal(errors.length,0,'No JavaScript runtime errors');
    console.log('PASS: browser login, form submission, size popup, focus and inert background, mobile width, dish detail, no JS errors.');
    console.log('Screenshots:',output);
} finally {await call('Page.close').catch(()=>{});ws.close();}
