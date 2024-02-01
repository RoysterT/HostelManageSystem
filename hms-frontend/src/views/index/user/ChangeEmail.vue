<script setup>

import {computed, reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {get, getStaffDetail, getStuDetail, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";
import {getUserGroup, getUserId} from "@/views/scripts/UserUtils";

const coldTime = ref(0)
const formRef = ref()

let userId = getUserId();
let identity = getUserGroup();
let userGroup = getUserGroup();
document.title = "更改邮箱 - HMS";

const userEmail = ref();
if (identity==="学生"){
  getStuDetail(userId, (data) => {
    userEmail.value = data.email;
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}
else {
  getStaffDetail(userId, (data) => {
    userEmail.value = data.email;
  }, (error) => {
    console.error("获取用户详情失败", error);
  });
}

const form = reactive({
  id: getUserId(),
  email: '',
  code: ''
})

const rule = {
  email: [
    {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
    {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: '请输入获取的验证码', trigger: 'blur'},
    {min: 6, max: 6, message: '验证码格式错误', trigger: ['blur', 'change']}
  ]
}

function askCode() {
  if (isEmailValid) {
    coldTime.value = 60
    get(`/api/auth/ask-code?email=${form.email}&type=change`, () => {
      ElMessage.success('验证码已发送，请注意查收')
      setInterval(() => coldTime.value--, 1000)
    }, (message) => {
      ElMessage.warning(message)
      coldTime.value = 0
    })
  } else {
    ElMessage.warning('请输入正确的电子邮件')
  }
}

const isEmailValid = computed(() =>
    /^\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/.test(form.email)
)

function changeEmail() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('/api/user/change-email', {...form}, () => {
        ElMessage.success("修改成功！")
        router.push('/user/info')
      })
    } else {
      ElMessage.warning("请填写完整表单内容")
    }
  })
}
</script>

<template>
  <div class="container">
    <div style="text-align: center">
      <div style="margin: 0 20px">
        <div style="margin-top: 80px">
          <div style="font-size: 25px;font-weight: bold">修改邮箱</div>
          <div style="font-size: 14px;color: grey">请输入改绑的电子邮箱地址</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rule" ref="formRef">
            <el-form-item prop="email">
              <el-input v-model="form.email" type="email" placeholder="电子邮箱">
                <template #prefix>
                  <el-icon>
                    <Message/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code" style="margin-top: 30px">
              <el-row :gutter="10" style="width: 100%">
                <el-col :span="19">
                  <el-input v-model="form.code" maxlength="6" type="text" placeholder="验证码">
                    <template #prefix>
                      <el-icon>
                        <EditPen/>
                      </el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="5">
                  <el-button @click="askCode" :disabled="!isEmailValid || coldTime > 0" type="success"
                             class="getCodeBtn">
                    {{ coldTime > 0 ? `${coldTime}秒后重发` : '获取验证码' }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 80px">
          <el-button @click="changeEmail" type="warning" plain style="width: 80%">完成修改</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import "./../../style/global.css";
@import "./style/reset.css";
</style>