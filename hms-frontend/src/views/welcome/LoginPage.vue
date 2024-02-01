<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import {reactive, ref} from "vue";
import {login} from "@/net";
import router from "@/router";

const formRef = ref();

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error("请输入学号或工号"))
  } else if (!/^\d{9}$|^\d{2}-\d{3}$/.test(value)) {
    callback(new Error("账号格式有误"))
  } else {
    callback()
  }
}

const rule = {
  username: [
    {validator: validateUsername, required: true, trigger: ['blur', 'change']}
  ],
  password: [
    {required: true, message: '请输入密码'},
  ]
}

function userLogin() {
  formRef.value.validate((valid) => {
    if (valid) {
      login(form.username, form.password, form.remember, () => router.push('/index'))
    }
  })
}
</script>

<template>
  <div style="text-align: center; margin: 0 20px">
    <div style="margin-top: 150px;font-weight: bold;font-size: 25px">登录</div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef" status-icon>
        <!-- 学号/工号 -->
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="9" type="text" placeholder="学号 / 工号">
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" maxlength="40" placeholder="密码">
            <template #prefix>
              <el-icon>
                <Lock/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row>
          <!-- 记住我 -->
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="记住我"/>
            </el-form-item>
          </el-col>
          <!-- 忘记密码 -->
          <el-col :span="12" style="text-align: right">
            <el-link @click="router.push('/reset')">忘记密码</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 40px;">
      <el-button @click="userLogin()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="font-size: 13px; color: grey">没有账号？</span>
    </el-divider>
    <div>
      <el-button @click="router.push('/register') " style="width: 270px" type="warning" plain>立即注册</el-button>
    </div>
  </div>
</template>

<style scoped>

</style>