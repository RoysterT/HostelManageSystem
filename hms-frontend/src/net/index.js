import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";

const authItemName = 'access_token';

/**
 * 默认错误
 * @param message 信息
 * @param code 错误码
 * @param url 请求地址
 */
const defaultFailure = (message, code, url) => {
    console.warn(`请求地址：${url}，状态码：${code}，错误信息：${message}`)
    ElMessage.warning(message)
}

/**
 * 复杂错误
 * @param err 错误信息
 */
const defaultError = (err) => {
    console.error(err);
    ElMessage.warning('发生了一些错误，请联系管理员')
    // console.warn(`请求地址：${url}，状态码：${code}，错误信息：${message}`)
    // ElMessage.warning(message)
}

/**
 * 获取Token
 * @returns {*|null}
 */
export function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    // 无token
    if (!str) {
        return null;
    }
    const authObj = JSON.parse(str);
    // 过期
    if (new Date(authObj.expire) <= new Date()) {
        ElMessage.warning('登录状态已过期，请重新登录！');
        // 3秒后跳转到登录页面
        setTimeout(() => {
            deleteAccessToken();
            router.push('/');
        }, 3000)
        return null;
    }
    return authObj.token;
}

/**
 * 存储Token
 * @param token Token字段
 * @param remember 记住Token
 * @param expire 过期时间
 * @param username 用户名
 * @param identity 用户组
 * @param id 用户id
 */
function storeAccessToken(token, remember, expire, username, identity, id) {
    const authObj = {
        token: token,
        expire: expire,
        username: username,
        identity: identity,
        id: id
    };
    const str = JSON.stringify(authObj);
    if (remember) {
        localStorage.setItem(authItemName, str);
    } else {
        sessionStorage.setItem(authItemName, str)
    }
}

/**
 * 删除Token
 */
function deleteAccessToken() {
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
}

/**
 * 获取请求头
 */
function accessHeader() {
    const token = takeAccessToken();

    return token ? {
        'Authorization': `Bearer ${takeAccessToken()}`
    } : {};
}

/**
 * 封装Post请求
 * @param url 请求地址
 * @param data 数据体
 * @param header 请求头
 * @param success 请求成功
 * @param failure 请求失败
 * @param error 请求错误
 */
function internalPost(url, data, header, success, failure, error = defaultError) {
    axios.post(url, data, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.msg, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * Post请求
 * @param url 请求地址
 * @param data 请求数据
 * @param success 请求成功
 * @param failure 请求失败
 */
function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure);
}

/**
 * 封装Get请求
 * @param url 请求地址
 * @param header 请求头
 * @param success 请求成功
 * @param failure 请求失败
 * @param error 请求错误
 */
function internalGet(url, header, success, failure, error = defaultError) {
    axios.get(url, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * Get请求
 * @param url 请求地址
 * @param success 请求成功
 * @param failure 请求失败
 */
function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure);
}

/**
 * 账户登录
 * @param username 用户名（id）
 * @param password 密码
 * @param remember 记住登录状态（储存Token）
 * @param success 登录成功
 * @param failure 登录失败
 */
function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(data.token, remember, data.expire, data.username, data.role, data.id)
        ElMessage.success(`成功，欢迎用户${data.username}登录！`)
        success(data)
    }, failure)
}

/**
 * 账户登出
 * @param success 登出成功
 * @param failure 登出失败
 */
function logout(success, failure = defaultFailure) {
    get('/api/auth/logout', () => {
        deleteAccessToken();
        ElMessage.success('退出登录成功！');
        success()
    }, failure)
}

/**
 * 获取学生账户数据
 * @param id 学生学号（账户名）
 * @param success 请求成功
 * @param failure 请求失败
 */
function getStuDetail(id, success, failure = defaultFailure) {
    get('/api/user/stu-info?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 获取职员账户信息
 * @param id 职员工号（用户名）
 * @param success 请求成功
 * @param failure 请求失败
 */
function getStaffDetail(id, success, failure = defaultFailure) {
    get('/api/user/staff-info?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员获取账户信息
 * @param currentPage 当前页
 * @param pageSize 页面大小
 * @param searchData 搜索数据
 * @param success 请求成功
 * @param failure 请求失败
 */
function getAccountInfo(currentPage, pageSize, searchData, success, failure = defaultFailure) {
    get('/api/manage/account/info?page=' + currentPage + '&pageSize='
        + pageSize + '&id=' + searchData.id + '&email=' + searchData.email, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员重置密码
 * @param id 账户id
 * @param success 请求成功
 * @param failure 请求失败
 */
function managerResetPassword(id, success, failure = defaultFailure) {
    get('/api/manage/account/reset-password?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员修改账户状态
 * @param id 账户id
 * @param status 账户状态
 * @param success 请求成功
 * @param failure 请求失败
 */
function managerChangeAccountStatus(id, status, success, failure = defaultFailure) {
    get('/api/manage/account/change-status?id=' + id + '&status=' + status, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除账户
 * @param id 账户id
 * @param success 请求成功
 * @param failure 请求失败
 */
function managerDeleteAccount(id, success, failure = defaultFailure) {
    get('/api/manage/account/delete?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员获取学生信息
 * @param currentPage 当前页
 * @param pageSize 页面大小
 * @param searchData 搜索数据
 * @param success 请求成功
 * @param failure 请求失败
 */
export function getStudentInfo(currentPage, pageSize, searchData, success, failure = defaultFailure) {
    get('/api/manage/student/info?page=' + currentPage
        + '&pageSize=' + pageSize
        + '&id=' + searchData.id
        + '&name=' + searchData.name
        + '&email=' + searchData.email
        + '&phoneNum=' + searchData.phoneNum
        + '&dept=' + searchData.dept
        + '&major=' + searchData.major
        + '&classId=' + searchData.classId, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除学生
 * @param id 学生id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerDeleteStudent(id, success, failure = defaultFailure) {
    get('/api/manage/student/delete?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员获取职员信息
 * @param currentPage 当前页
 * @param pageSize 页面大小
 * @param searchData 搜索数据
 * @param success 请求成功
 * @param failure 请求失败
 */
export function getStaffInfo(currentPage, pageSize, searchData, success, failure = defaultFailure) {
    get('/api/manage/staff/info?page=' + currentPage
        + '&pageSize=' + pageSize
        + '&id=' + searchData.id
        + '&name=' + searchData.name
        + '&email=' + searchData.email
        + '&phoneNum=' + searchData.phoneNum
        + '&manageBuilding=' + searchData.manageBuilding, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除职员
 * @param id 职员id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerDeleteStaff(id, success, failure = defaultFailure) {
    get('/api/manage/staff/delete?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员获取住宿信息
 * @param currentPage 当前页
 * @param pageSize 页面大小
 * @param searchData 搜索数据
 * @param success 请求成功
 * @param failure 请求失败
 */
export function getAccommodationInfo(currentPage, pageSize, searchData, success, failure = defaultFailure) {
    get('/api/manage/accommodation/info?page=' + currentPage
        + '&pageSize=' + pageSize
        + '&id=' + searchData.id
        + '&building=' + searchData.building
        + '&unit=' + searchData.unit
        + '&room=' + searchData.room
        + '&bed=' + searchData.bed, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除住宿信息
 * @param id 住宿id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerDeleteAccommodation(id, success, failure = defaultFailure) {
    get('/api/manage/accommodation/delete?id=' + id, (data) => {
        success(data)
    }, failure)
}

export function getUserAccommodation(id, success, failure = defaultFailure) {
    get('/api/user/accommodation?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 系统管理员获取所有用户组信息
 * @param currentPage 当前页
 * @param pageSize 页面大小
 * @param success 请求成功
 * @param failure 请求失败
 */
export function getIdentityInfo(currentPage, pageSize, success, failure = defaultFailure) {
    get('/api/system/permission/all?page=' + currentPage
        + '&pageSize=' + pageSize, (data) => {
        success(data)
    }, failure)
}

/**
 * 获得对应id的用户组信息
 * @param id
 * @param success
 * @param failure
 */
export function getIdIdentity(id, success, failure = defaultFailure) {
    get('/api/system/permission/info/' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 获得所有用户组信息（用于表单选择）
 * @param success 成功
 * @param failure 失败
 */
export function getIdentityList(success, failure = defaultFailure) {
    get('/api/system/permission/array', (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除用户组
 * @param id 用户组id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerDeleteIdentity(id, success, failure = defaultFailure) {
    get('/api/system/permission/delete?id=' + id, (data) => {
        success(data)
    }, failure)
}

/**
 * 获得用户组权限
 * @param userIdentity 用户组id
 * @param success 成功
 * @param failure 失败
 */
export function getIdentityPermission(userIdentity, success, failure = defaultFailure){
    get("/api/system/permission/info/" + userIdentity, (data) => {
        success(data)
    }, failure)
}

/**
 * 获得是否是未认证状态
 * @returns {boolean}
 */
function unauthorized() {
    return !takeAccessToken();
}

/**
 * 获得token是否过期
 * @returns {boolean} true为过期，false为未过期
 */
function isTokenExpired() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    // 无token
    if (!str) {
        return true;
    }
    const authObj = JSON.parse(str);
    // 过期
    if (new Date(authObj.expire) <= new Date()) {
        return true;
    }
    return false;
}

/**
 * 管理员关闭报修
 * @param repairOrder 报修id
 * @param setType 关闭类型
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerCloseRepair(repairOrder, setType, success, failure = defaultFailure) {
    get('/api/repair/close?repairOrder=' + repairOrder + '&setType=' + setType, (data) => {
        success(data)
    }, failure)
}

/**
 * 根据repairOrder查询报修信息
 * @param repairOrder 报修id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function getRepairInfoByOrder(repairOrder, success, failure = defaultFailure) {
    get('/api/repair/info/' + repairOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 楼栋管理员确认报修单
 * @param repairOrder 报修id
 * @param success 请求成功
 * @param failure 请求失败
 */
export function confirmRepairWithOrder(repairOrder, success, failure = defaultFailure) {
    get('/api/repair/confirm?repairOrder=' + repairOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 维修人员完成维修
 * @param repairOrder 报修id
 * @param workerName 维修人员姓名
 * @param success 请求成功
 * @param failure 请求失败
 */
export function toFinishRepair(repairOrder, workerName, success, failure = defaultFailure) {
    get('/api/repair/finish-repair?repairOrder=' + repairOrder + '&worker=' + workerName, (data) => {
        success(data)
    }, failure)
}

/**
 * 学生
 * @param repairOrder
 * @param success
 * @param failure
 */
export function confirmFinishRepair(repairOrder, success, failure = defaultFailure) {
    get('/api/repair/confirm-finish?repairOrder=' + repairOrder, (data) => {
        success(data)
    }, failure)
}

export function getRepairDetail(repairOrder, success, failure = defaultFailure) {
    get('/api/repair/info/' + repairOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 管理员删除缴费信息
 * @param paymentId 缴费单号
 * @param success 请求成功
 * @param failure 请求失败
 */
export function managerDeletePayment(paymentOrder, success, failure = defaultFailure) {
    get('/api/payment/delete?paymentOrder=' + paymentOrder, (data) => {
        success(data)
    }, failure)
}

export function getPaymentInfo(paymentId, success, failure = defaultFailure) {
    get('/api/payment/info/id/' + paymentId, (data) => {
        success(data)
    }, failure)
}

export function confirmPayment(paymentId, success, failure = defaultFailure) {
    get('/api/payment/confirm?paymentId=' + paymentId, (data) => {
        success(data)
    }, failure)
}

/**
 * 获取换宿申请详情
 * @param recordOrder 换宿申请单号
 * @param success 成功
 * @param failure 失败
 */
export function getAccommodationChangeInfoByOrder(recordOrder, success, failure = defaultFailure) {
    get('/api/dorm/apply/info/order/' + recordOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 楼栋管理员确认换宿申请
 * @param recordOrder 换宿申请单号
 * @param success 成功
 * @param failure 失败
 */
export function confirmAccommodationChangeWithOrder(recordOrder, success, failure = defaultFailure) {
    get('/api/dorm/building/confirm/' + recordOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 学校通过换宿申请
 * @param recordOrder 换宿申请记录序号
 * @param success 成功
 * @param failure 失败
 */
export function managerConfirmAccommodationChange(recordOrder, success, failure = defaultFailure) {
    get('/api/dorm/manage/confirm/' + recordOrder, (data) => {
        success(data)
    }, failure)
}

/**
 * 拒绝换宿申请
 * @param recordOrder 换宿申请单号
 * @param success 成功
 * @param failure 失败
 */
export function managerRejectAccommodationChange(recordOrder, progress, success, failure = defaultFailure) {
    get('/api/dorm/building/reject/' + recordOrder
        + '?progress=' + progress, (data) => {
        success(data)
    }, failure)
}

/**
 * 删除卫生检查活动
 * @param activityId 活动id
 * @param success 成功
 * @param failure 失败
 */
export function managerDeleteActivity(activityId, success, failure = defaultFailure) {
    get('/api/hygiene/activity/delete/' + activityId, (data) => {
        success(data)
    }, failure)
}

/**
 * 获取卫生检查活动详情
 * @param activityId 活动id
 * @param success 成功
 * @param failure 失败
 */
export function getActivityInfo(activityId, success, failure = defaultFailure) {
    get('/api/hygiene/activity/info/' + activityId, (data) => {
        success(data)
    }, failure)
}

/**
 * 获取卫生检查记录详情
 * @param recordId 记录id
 * @param success 成功
 * @param failure 失败
 */
export function managerDeleteHygiene(recordId, success, failure = defaultFailure) {
    get('/api/hygiene/mark/delete/' + recordId, (data) => {
        success(data)
    }, failure)
}

/**
 * 获取卫生检查记录详情
 * @param recordId 记录id
 * @param success 成功
 * @param failure 失败
 */
export function getHygieneInfo(recordId, success, failure = defaultFailure) {
    get('/api/hygiene/mark/info/' + recordId, (data) => {
        success(data)
    }, failure)
}

/**
 * 获得可选活动列表
 * @param success 成功
 * @param failure 失败
 */
export function getActivityArray(success, failure = defaultFailure) {
    get('/api/hygiene/activity/all/array-active', (data) => {
        success(data)
    }, failure)
}

export function getAllActivityArray(success, failure = defaultFailure) {
    get('/api/hygiene/activity/all/array', (data) => {
        success(data)
    }, failure)
}

export {
    login, logout, internalGet, get, internalPost, post, unauthorized, accessHeader,
    getStuDetail, getStaffDetail, getAccountInfo, managerResetPassword,
    managerChangeAccountStatus, managerDeleteAccount, isTokenExpired
}