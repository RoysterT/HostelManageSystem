<script setup>

import {ref} from "vue";
import {getStaffDetail, getStuDetail} from "@/net";
import {getUserGroup, getUserId, getUserName} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();
let userName = getUserName();
let userId = getUserId();

document.title = "用户信息 - HMS";

const email = ref(null);
const id = ref(null);
const gender = ref(null);
const phone = ref(null);
const dept = ref(null);
const major = ref(null);
const classId = ref(null);
const admission = ref(null);
const manageBuilding = ref(null);

// 显示 学号 or 工号
const idName = ref(null);

// 调用接口并在成功时更新变量
if (userGroup === "学生"){
  getStuDetail(userId, (data) => {
    email.value = data.email;
    id.value = data.id;
    gender.value = data.gender;
    phone.value = data.phoneNum;
    dept.value = data.dept;
    major.value = data.major;
    classId.value = data.classId;
    admission.value = data.admission;
    idName.value = "学号";
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}
else {
  getStaffDetail(userId, (data) => {
    email.value = data.email;
    id.value = data.id;
    gender.value = data.gender;
    phone.value = data.phoneNum;
    manageBuilding.value = data.manageBuilding;
    idName.value = "工号";
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}

const hasAccommodation = true;

</script>

<template>
  <div class="main-container">
    <div class="avatar-container">
      <div class="avatar-part">
        <img src="./../../img/avatar.png" class="avatar"/>
      </div>
      <div class="name-part">
        <div class="name-name cur-pointer" title="用户名">{{ userName }}</div>
        <div class="name-group cur-pointer" title="所属用户组">{{ userGroup }}</div>
      </div>
    </div>
    <el-divider class="divider"/>
    <div class="item-wrapper">
      <div class="item">
        <el-row>
          <el-col :span="8" class="item-key">邮箱</el-col>
          <el-col :span="16" class="item-value">{{ email || "未设置" }}</el-col>
        </el-row>
      </div>
<!--      <el-divider class="divider"/>-->
      <div class="item">
        <el-row>
          <el-col :span="8" class="item-key">{{ idName }}</el-col>
          <el-col :span="16" class="item-value">{{ id || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item">
        <el-row>
          <el-col :span="8" class="item-key">性别</el-col>
          <el-col :span="16" class="item-value">{{ gender || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item">
        <el-row>
          <el-col :span="8" class="item-key">手机号</el-col>
          <el-col :span="16" class="item-value">{{ phone || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item" v-if='userGroup === "学生"'>
        <el-row>
          <el-col :span="8" class="item-key">学院</el-col>
          <el-col :span="16" class="item-value">{{ dept || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item" v-if='userGroup === "学生"'>
        <el-row>
          <el-col :span="8" class="item-key">专业</el-col>
          <el-col :span="16" class="item-value">{{ major || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item" v-if='userGroup === "学生"'>
        <el-row>
          <el-col :span="8" class="item-key">班级代码</el-col>
          <el-col :span="16" class="item-value">{{ classId || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item" v-if='userGroup === "学生"'>
        <el-row>
          <el-col :span="8" class="item-key">入学年份</el-col>
          <el-col :span="16" class="item-value">{{ admission || "未设置" }}</el-col>
        </el-row>
      </div>
      <div class="item" v-if='userGroup === "楼栋管理员"'>
        <el-row>
          <el-col :span="8" class="item-key">管理楼栋</el-col>
          <el-col :span="16" class="item-value">{{ manageBuilding || "未设置" }}</el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import "./../../style/global.css";
@import "./style/info.css";
</style>