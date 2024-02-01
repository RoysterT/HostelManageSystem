<script setup>

import {getUserGroup, getUserId, getUserIdentity} from "@/views/scripts/UserUtils";
import {reactive, ref} from "vue";
import {Iphone} from "@element-plus/icons-vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

let userId = getUserId();
let identity = getUserIdentity();
const formRef = ref();
const userGroup = getUserGroup();
document.title = "修改手机号 - HMS";

const validatePhoneNum = (rule, value, callback) => {
  if (value === ''){
    callback(new Error("请输入手机号码"))
  } else if (!/((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|192|198|199|(147))\d{8}/.test(value)) {
    callback(new Error("手机号码格式错误"))
  } else {
    callback()
  }
}

const form = reactive({
  id: userId,
  identity: identity,
  phone: ''
})

const rule = {
  phone: [
    {required: true, message: '请输入手机号码', trigger: 'blur'},
    {validator: validatePhoneNum, message: '请输入正确的手机号码', trigger: ['blur', 'change']}
  ]
}

function changePhone() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('/api/user/change-phone', {...form}, () => {
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
          <div style="font-size: 25px;font-weight: bold">修改手机号</div>
          <div style="font-size: 14px;color: grey">请输入新绑定的手机号码</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rule" ref="formRef">
            <el-form-item prop="phone">
              <el-input v-model="form.phone" minlength="11" maxlength="11" placeholder="手机号码">
                <template #prefix>
                  <el-icon>
                    <Iphone />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 80px">
          <el-button @click="changePhone" type="warning" plain style="width: 80%">完成修改</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import "./../../style/global.css";
@import "./style/reset.css";
</style>