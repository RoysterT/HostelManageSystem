<script setup>
import {ref} from "vue";
import {getStaffDetail, getStuDetail} from "@/net";
import {getUserGroup, getUserId, getUserName} from "@/views/scripts/UserUtils";
import router from "@/router";

let userGroup = getUserGroup();
let userName = getUserName();
let userId = getUserId();
document.title = "信息修改" + " - HMS";

const email = ref(null);
const phone = ref(null);

// 调用接口并在成功时更新变量
if (userGroup === "学生"){
  getStuDetail(userId, (data) => {
    email.value = data.email;
    phone.value = data.phoneNum;
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}
else {
  getStaffDetail(userId, (data) => {
    email.value = data.email;
    phone.value = data.phoneNum;
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}
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
          <el-col :span="7" class="item-key">邮箱</el-col>
          <el-col :span="14" class="item-value">{{ email || "未设置" }}</el-col>
          <el-col :span="3" class="item-change">
            <el-button link @click="router.push('/user/changeEmail')">修改邮箱</el-button>
          </el-col>
        </el-row>
      </div>
      <div class="item">
        <el-row>
          <el-col :span="7" class="item-key">手机号</el-col>
          <el-col :span="14" class="item-value">{{ phone || "未设置" }}</el-col>
          <el-col :span="3" class="item-change">
            <el-button link @click="router.push('/user/changePhone')">修改手机号</el-button>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import "./../../style/global.css";
@import "./style/info.css";
@import "./style/change.css";
</style>