'use strict';

// Khi popup mở, chỉ cho phép thao tác bằng bàn phím bên trong popup.
const modal = document.querySelector('[data-modal]');
if (modal) {
    const main = document.querySelector('main');
    const backdrop = modal.parentElement;

    for (const child of main.children) {
        if (child !== backdrop && child.tagName !== 'SCRIPT') {
            child.inert = true;
        }
    }

    const selector = 'button:not([disabled]), a[href], input:not([type=hidden])';
    const controls = Array.from(modal.querySelectorAll(selector));
    if (controls.length > 0) {
        controls[0].focus();
    }

    modal.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            const cancelLink = modal.querySelector('[data-modal-cancel]');
            window.location.assign(cancelLink.href);
        }

        if (event.key !== 'Tab' || controls.length === 0) {
            return;
        }

        const firstControl = controls[0];
        const lastControl = controls[controls.length - 1];

        if (event.shiftKey && document.activeElement === firstControl) {
            event.preventDefault();
            lastControl.focus();
        } else if (!event.shiftKey && document.activeElement === lastControl) {
            event.preventDefault();
            firstControl.focus();
        }
    });
}

// Kiểm tra nhanh ngày kết thúc trên trình duyệt; Servlet vẫn kiểm tra lại dữ liệu.
const startDate = document.querySelector('#startDate');
const endDate = document.querySelector('#endDate');

if (startDate && endDate) {
    function validateDates() {
        let message = '';
        if (startDate.value && endDate.value && endDate.value < startDate.value) {
            message = 'Ngày kết thúc phải từ ngày bắt đầu trở đi.';
        }
        endDate.setCustomValidity(message);
    }

    startDate.addEventListener('change', validateDates);
    endDate.addEventListener('change', validateDates);
}
