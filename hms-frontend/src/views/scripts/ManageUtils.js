import {ElMessage} from "element-plus";

/**
 * 复制到剪贴板
 * @param content
 */
export function copy(content){
    const input = document.createElement('input');
    input.setAttribute('readonly', 'readonly');
    input.setAttribute('value', content);
    document.body.appendChild(input);
    input.select();
    if (document.execCommand('copy')) {
        document.execCommand('copy');
        ElMessage.success("复制成功");
    }
    document.body.removeChild(input);
}