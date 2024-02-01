/**
 * UserUtils.js
 * 处理用于提供服务的基本用户数据
 */

import {getStaffDetail, logout} from "@/net";
import {ref} from "vue";
import router from "@/router";

/**
 * 获取用户问候词
 * @returns {string} 用户问候词
 */
export function getUserGreets() {
    const now = new Date();
    const currentHour = now.getHours();
    if (currentHour >= 6 && currentHour < 12) {
        return "上午好";
    } else if (currentHour >= 12 && currentHour < 14) {
        return "中午好";
    } else if (currentHour >= 14 && currentHour < 18) {
        return "下午好";
    } else {
        return "晚上好";
    }
}

/**
 * 用户登出
 */
export function userLogOut() {
    logout(() => router.push('/'));
}

/**
 * 获得存储在Session中的Token数据json
 * @returns { json | null }
 */
export function getTokenData() {
// 从LocalStorage中获取access_token
    const tokenString = localStorage.getItem('access_token')
        || sessionStorage.getItem('access_token');
    if (tokenString) {
        return JSON.parse(tokenString);
    }
    return null;
}

/**
 * 获取用户用户组id
 * @returns {number} 用户组id
 */
export function getUserIdentity() {
    const tokenData = getTokenData();
    if (tokenData) {
        return tokenData.identity;
    }
    return 0;
}

/**
 * 从Token数据中获取用户组
 * @returns {string} 用户组字段
 */
export function getUserGroup() {
    const tokenData = getTokenData();
    if (tokenData) {
        switch (tokenData.identity) {
            case 0:
                return "新建用户组";
            case 1:
                return "系统管理员"
            case 2:
                return "账户信息管理员";
            case 3:
                return "学生";
            case 4:
                return "楼栋管理员";
            case 5:
                return "水电工作人员";
            case 6:
                return "订水人员";
            case 7:
                return "维修工作人员";
            default:
                return "未知分组";
        }
    }
    return "未知分组";
}

/**
 * 获得identity对应的用户组字符串
 * @param identity 用户组id
 * @returns {string} 用户组字符串
 */
export function getUserGroupString(identity){
    switch (identity) {
        case 0:
            return "新建用户组";
        case 1:
            return "系统管理员"
        case 2:
            return "账户信息管理员";
        case 3:
            return "学生";
        case 4:
            return "楼栋管理员";
        case 5:
            return "水电工作人员";
        case 6:
            return "订水人员";
        case 7:
            return "维修工作人员";
        default:
            return "未知分组";
    }
}

export function getIdentityWithArr(){
    
}

/**
 * 从Token数据中获取用户名
 * @returns { string } 用户名
 */
export function getUserName() {
    const tokenData = getTokenData();
    if (tokenData) {
        return tokenData.username;
    }
    return "未知用户名";
}

/**
 * 从Token数据中获取用户id
 * @returns {string}
 */
export function getUserId() {
    const tokenData = getTokenData();
    if (tokenData) {
        return tokenData.id;
    }
    return "未知用户id";
}

export function getManageBuilding(userId){
    const building = ref("");
    getStaffDetail(userId, (data) => {
        building.value =  data.building;
    }, (error) => {
        console.error("获取管理楼栋信息失败", error);
    });
    return building;
}