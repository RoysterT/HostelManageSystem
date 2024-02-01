<script setup>
import {getUserGreets, getUserGroup, getUserIdentity, getUserName, userLogOut} from "./scripts/UserUtils";
import router from "@/router";
import {getIdentityPermission} from "@/net";
import {ref} from "vue";
import {Help, MessageBox, Money, Setting, User, House, Brush, Goods} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";

let userGreets = getUserGreets();
let userName = getUserName();
let userIdentity = getUserIdentity();
let staticUserGroup = getUserGroup();

const userInfo = ref(false);
const userEdit = ref(false);
const userPassword = ref(false);
const userEmail = ref(false);
const userPhone = ref(false);
const menu_user = ref(false);

const accommodation_info = ref(false);
const accommodation_apply = ref(false);
const accommodation_audit = ref(false);
const accommodation_confirm = ref(false);
const menu_accommodation = ref(false);

const inspectRules = ref(false);
const inspectScore = ref(false);
const inspectMark = ref(false);
const inspectActivity = ref(false);
const menu_inspect = ref(false);

const maintenanceApply = ref(false);
const maintenanceHistory = ref(false);
const maintenanceBuilding = ref(false);
const maintenanceManage = ref(false);
const menu_maintenance = ref(false);

const paymentBill = ref(false);
const paymentHistory = ref(false);
const paymentBuilding = ref(false);
const paymentManage = ref(false);
const menu_payment = ref(false);

const waterBill = ref(false);
const waterHistory = ref(false);
const waterSending = ref(false);
const waterManage = ref(false);
const menu_water = ref(false);

const systemPermission = ref(false);
const systemAssignment = ref(false);
const menu_system = ref(false);

const manageAccount = ref(false);
const manageStudent = ref(false);
const manageStaff = ref(false);
const manageAccommodation = ref(false);
const menu_manage = ref(false);

getIdentityPermission(userIdentity, (data) => {
    userInfo.value = data.userInfo;
    userEdit.value = data.userEdit;
    userPassword.value = data.userPassword;
    userEmail.value = data.userEmail;
    userPhone.value = data.userPhone;
    menu_user.value = data.userInfo || data.userEdit || data.userPassword || data.userEmail || data.userPhone;
    accommodation_info.value = data.accommodationInfo;
    accommodation_apply.value = data.accommodationApply;
    accommodation_audit.value = data.accommodationAudit;
    accommodation_confirm.value = data.accommodationConfirm;
    menu_accommodation.value = data.accommodationInfo || data.accommodationApply || data.accommodationAudit || data.accommodationConfirm;
    inspectRules.value = data.inspectRules;
    inspectScore.value = data.inspectScore;
    inspectMark.value = data.inspectMark;
    inspectActivity.value = data.inspectActivity;
    menu_inspect.value = data.inspectRules || data.inspectScore || data.inspectMark || data.inspectActivity;
    maintenanceApply.value = data.maintenanceApply;
    maintenanceHistory.value = data.maintenanceHistory;
    maintenanceBuilding.value = data.maintenanceBuilding;
    maintenanceManage.value = data.maintenanceManage;
    menu_maintenance.value = data.maintenanceApply || data.maintenanceHistory || data.maintenanceBuilding || data.maintenanceManage;
    paymentBill.value = data.paymentBill;
    paymentHistory.value = data.paymentHistory;
    paymentBuilding.value = data.paymentBuilding;
    paymentManage.value = data.paymentManage;
    menu_payment.value = data.paymentBill || data.paymentHistory || data.paymentBuilding || data.paymentManage;
    waterBill.value = data.waterBill;
    waterHistory.value = data.waterHistory;
    waterSending.value = data.waterSending;
    waterManage.value = data.waterManage;
    menu_water.value = data.waterBill || data.waterHistory || data.waterSending || data.waterManage;
    systemPermission.value = data.systemPermission;
    systemAssignment.value = data.systemAssignment;
    menu_system.value = data.systemPermission || data.systemAssignment;
    manageAccount.value = data.manageAccount;
    manageStudent.value = data.manageStudent;
    manageStaff.value = data.manageStaff;
    manageAccommodation.value = data.manageAccommodation;
    menu_manage.value = data.manageAccount || data.manageStudent || data.manageStaff || data.manageAccommodation;
}, (error) => {
    console.error("获取用户组失败", error);
    ElMessage.error("用户组获取失败！");
})

import {getIdentityList} from "@/net";
let userGroup = ref(null);
const identityList = ref([]);
let userGroupVisible = ref(false);
// 将从getIdentityList获得的json对象的数据部分存储为下拉选择框的数据数组
getIdentityList((data) => {
    identityList.value = data;
    const userIdentity = getUserIdentity();
    userGroup.value = identityList.value.find((item) => {
        return item.value === userIdentity;
    }).label;
    document.title = "首页" + " - HMS(" + userGroup.value + ")";
    staticUserGroup = userGroup.value;
    userGroupVisible.value = true;
}, (error) => {
    console.error("获取用户组列表失败", error);
});
</script>

<template>
    <div class="common-layout">
        <el-container style="height: 100vh; overflow: hidden">
            <el-header>
                <el-link href="/index" :underline="false">
                    <div class="header-title cur-pointer">
                        <span title="点击返回首页"> 学生宿舍管理系统 </span>
                    </div>
                </el-link>
                <div class="header-right" v-if="userGroupVisible">
                    <div class="user-welcome">
                        <span id="userGreets">{{ userGreets }}</span>，
                        <span id="userName">{{ userName }}</span>（
                        <span id="userGroup">{{ staticUserGroup }}</span> ）！
                    </div>
                    <div class="header-logout">
                        <el-button class="logout-btn" type="warning" @click="userLogOut">退出登录</el-button>
                    </div>
                </div>
            </el-header>
            <el-container>
                <el-aside style="height: 92vh; overflow-y: scroll;">
                    <el-menu class="el-menu-vertical-demo menu-wrapper">
                        <el-sub-menu index="1" v-if="menu_user">
                            <template #title>
                                <el-icon>
                                    <User/>
                                </el-icon>
                                <span>账号设置</span>
                            </template>
                            <el-menu-item index="1-1" @click="router.push('/user/info')" v-if="userInfo">用户信息</el-menu-item>
                            <el-menu-item index="1-2" @click="router.push('/user/edit')" v-if="userEdit">信息修改</el-menu-item>
                            <el-menu-item index="1-3" @click="router.push('/user/reset')" v-if="userPassword">密码重置</el-menu-item>
                            <!--<el-menu-item index="1-4" @click="router.push('/user/test')">路由测试页面</el-menu-item>-->
                        </el-sub-menu>
                        <el-sub-menu index="2" v-if="menu_accommodation">
                            <template #title>
                                <el-icon>
                                    <House/>
                                </el-icon>
                                <span>住宿管理</span>
                            </template>
                            <el-menu-item index="2-1" @click="router.push('/accommodation/info')" v-if="accommodation_info">住宿信息</el-menu-item>
                            <el-menu-item index="2-2" @click="router.push('/accommodation/apply')" v-if="accommodation_apply">换宿申请</el-menu-item>
                            <el-menu-item index="2-3" @click="router.push('/accommodation/audit')" v-if="accommodation_audit">换宿确认</el-menu-item>
                            <el-menu-item index="2-4" @click="router.push('/accommodation/confirm')" v-if="accommodation_confirm">换宿审核</el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="3" v-if="menu_inspect">
                            <template #title>
                                <el-icon>
                                    <Brush/>
                                </el-icon>
                                <span>卫生管理</span>
                            </template>
                            <el-menu-item index="3-1" @click="router.push('/inspect/rules')" v-if="inspectRules">卫生规范条例</el-menu-item>
                            <el-menu-item index="3-2" @click="router.push('/inspect/score')" v-if="inspectScore">宿舍卫生评级</el-menu-item>
                            <el-menu-item index="3-3" @click="router.push('/inspect/mark')" v-if='inspectMark'>卫生评分</el-menu-item>
                            <el-menu-item index="3-4" @click="router.push('/inspect/activity')" v-if="inspectActivity">卫生活动管理</el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="4" v-if="menu_maintenance">
                            <template #title>
                                <el-icon>
                                    <Help/>
                                </el-icon>
                                <span>宿舍报修</span>
                            </template>
                            <el-menu-item index="4-1" @click="router.push('/maintenance/apply')" v-if="maintenanceApply">提交报修</el-menu-item>
                            <el-menu-item index="4-2" @click="router.push('/maintenance/history')" v-if="maintenanceHistory">宿舍往期报修</el-menu-item>
                            <el-menu-item index="4-3" @click="router.push('/maintenance/building')" v-if="maintenanceBuilding">楼栋报修信息</el-menu-item>
                            <el-menu-item index="4-4" @click="router.push('/maintenance/manage')" v-if="maintenanceManage">报修管理</el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="5" v-if="menu_payment">
                            <template #title>
                                <el-icon>
                                    <Money/>
                                </el-icon>
                                <span>缴费服务</span>
                            </template>
                            <el-menu-item index="5-1" @click="router.push('/payment/bill')" v-if="paymentBill">水电缴费</el-menu-item>
                            <el-menu-item index="5-2" @click="router.push('/payment/history')" v-if="paymentHistory">历史缴费</el-menu-item>
                            <el-menu-item index="5-3" @click="router.push('/payment/building')" v-if="paymentBuilding">楼栋缴费信息</el-menu-item>
                            <el-menu-item index="5-4" @click="router.push('/payment/manage')" v-if="paymentManage">缴费管理</el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="6" v-if="menu_water">
                            <template #title>
                                <el-icon>
                                    <Goods/>
                                </el-icon>
                                <span>饮用水订购</span>
                            </template>
                            <el-menu-item index="6-1" @click="router.push('/water/bill')" v-if="waterBill">饮用水订购</el-menu-item>
                            <el-menu-item index="6-2" @click="router.push('/water/history')" v-if="waterHistory">订购记录</el-menu-item>
                            <el-menu-item index="6-3" @click="router.push('/water/sending')" v-if="waterSending">订购管理（送水）</el-menu-item>
                            <el-menu-item index="6-4" @click="router.push('/water/manage')" v-if="waterManage">订购管理（信息）</el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="7" v-if="menu_system">
                            <template #title>
                                <el-icon>
                                    <Setting/>
                                </el-icon>
                                <span>系统维护</span>
                            </template>
                            <el-menu-item index="7-1" @click="router.push('/system/permission')" v-if="systemPermission">用户组权限设置
                            </el-menu-item>
                            <!-- <el-menu-item index="7-2" @click="router.push('')">系统功能启停</el-menu-item> -->
                            <el-menu-item index="7-3" @click="router.push('/system/assignment')" v-if="systemAssignment">用户所属组管理
                            </el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="8" v-if="menu_manage">
                            <template #title>
                                <el-icon>
                                    <MessageBox/>
                                </el-icon>
                                <span>账户管理</span>
                            </template>
                            <el-menu-item index="8-1" @click="router.push('/manage/account')" v-if="manageAccount">账号数据</el-menu-item>
                            <el-menu-item index="8-2" @click="router.push('/manage/student')" v-if="manageStudent">学生数据</el-menu-item>
                            <el-menu-item index="8-3" @click="router.push('/manage/staff')" v-if="manageStaff">职工数据</el-menu-item>
                            <el-menu-item index="8-4" @click="router.push('/manage/accommodation')" v-if="manageAccommodation">住宿数据</el-menu-item>
                        </el-sub-menu>
                    </el-menu>
                </el-aside>
                <el-main style="height: 92vh; overflow-y: scroll; padding: 0">
                    <router-view/>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<style scoped>
@import "@/views/style/global.css";
@import "@/views/style/index-page.css";
@import "@/views/style/index.css";
</style>