<script setup>

import {computed, reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {get, getStaffDetail, getStuDetail, logout, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";
import {getUserGroup, getUserId} from "@/views/scripts/UserUtils";

const coldTime = ref(0)
const active = ref(0)
const formRef = ref()
document.title = "重置密码" + " - HMS";

let userId = getUserId();
let identity = getUserGroup();

const userEmail = ref();
if (identity === "学生") {
    getStuDetail(userId, (data) => {
        userEmail.value = data.email;
    }, (error) => {
        console.error("获取用户详情失败", error);
    });
} else {
    getStaffDetail(userId, (data) => {
        userEmail.value = data.email;
    }, (error) => {
        console.error("获取用户详情失败", error);
    });
}

const form = reactive({
    email: userEmail,
    code: '',
    password: '',
    password_repeat: ''
})

const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请再次输入密码"))
    } else if (value !== form.password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}

const rule = {
    email: [
        {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
    ],
    code: [
        {required: true, message: '请输入获取的验证码', trigger: 'blur'},
        {min: 6, max: 6, message: '验证码格式错误', trigger: ['blur', 'change']}
    ],
    password: [
        {required: true, message: '请输入密码', trigger: 'blur'},
        {min: 6, max: 50, message: '密码长度过短或过长', trigger: ['blur', 'change']}
    ],
    password_repeat: [
        {validator: validatePassword, trigger: ['blur', 'change']}
    ]
}

function askCode() {
    if (isEmailValid) {
        coldTime.value = 60
        get(`/api/auth/ask-code?email=${form.email}&type=reset`, () => {
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

const isEmailValid = true/*computed(() =>
    /^\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/.test(form.email)
)*/

function confirmReset() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/auth/reset-confirm', {
                email: form.email,
                code: form.code
            }, () => active.value++)
        }
    })
}

function doReset() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/auth/reset-password', {...form}, () => {
                ElMessage.success("密码重置成功，请重新登录")
                router.push('/')
            })
        }
    })
}
</script>

<template>
    <div class="container">
        <div style="text-align: center">
            <div style="margin-top: 30px">
                <el-steps :active="active" finish-status="success" align-center>
                    <el-step title="验证电子邮件"/>
                    <el-step title="设置新密码"/>
                </el-steps>
            </div>
            <div style="margin: 0 20px" v-if="active === 0">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请输入账户电子邮箱地址</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rule" ref="formRef">
                        <el-form-item prop="email">
                            <el-input :v-model="form.email" type="email" placeholder="电子邮箱" :value="userEmail"
                                      readonly>
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
                    <el-button @click="confirmReset" type="warning" plain style="width: 80%">开始重置密码</el-button>
                </div>
            </div>
            <div style="margin:0 20px" v-if="active === 1">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请重新设置账户密码</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rule" ref="formRef">
                        <el-form-item prop="password">
                            <el-input v-model="form.password" maxlength="50" type="password" placeholder="密码">
                                <template #prefix>
                                    <el-icon>
                                        <Lock/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password_repeat" style="margin-top: 30px">
                            <el-input v-model="form.password_repeat" maxlength="50" type="password"
                                      placeholder="重复密码">
                                <template #prefix>
                                    <el-icon>
                                        <Lock/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 80px">
                    <el-button @click="doReset" type="danger" plain style="width: 80%">提交重置</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import "./../../style/global.css";
@import "./style/reset.css";
</style>