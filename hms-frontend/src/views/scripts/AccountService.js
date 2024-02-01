/**
 * AccountService.js
 * 账户服务相关
 */
import {getUserGroup, getUserId} from "@/views/scripts/UserUtils";
import {getStaffDetail, getStuDetail} from "@/net";

/**
 * 获取当前账户用户信息
 * @return 用户数据
 */
export async function getUserInfo() {
    const userData = {
        admission: 0,
        classId: 0,
        dept: "",
        email: "",
        gender: "",
        graduation: 0,
        id: "",
        major: "",
        name: "",
        phoneNum: "",
        manageBuilding: ""
    }
    if (getUserGroup() === "学生" || getUserGroup() === 3) {
        getStuDetail(getUserId(), (data) => {
            console.log(data);
            // return data;
            userData.admission = data.admission;
            userData.classId = data.classId;
            userData.dept = data.dept;
            userData.email = data.email;
            userData.gender = data.gender;
            userData.graduation = data.graduation;
            userData.id = data.id;
            userData.major = data.major;
            userData.name = data.name;
            userData.phoneNum = data.phoneNum;
        }, (error) => {
            console.error("获取用户详情失败", error);
        });
    }
    else {
        getStaffDetail(getUserId(), (data) => {
            userData.email = data.email;
            userData.gender = data.gender;
            userData.id = data.id;
            userData.name = data.name;
            userData.phoneNum = data.phoneNum;
            userData.manageBuilding = data.manageBuilding;
        }, (error) => {
            console.error("获取用户详情失败", error);
        });
    }
    return userData;
}
