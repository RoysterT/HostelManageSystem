<script setup>

import {getUserGroup, getUserId, getUserName} from "@/views/scripts/UserUtils";
import {getStaffDetail, getStuDetail, post} from "@/net";
import {reactive, ref} from "vue";
import {Back, CircleCheck, DArrowLeft, DArrowRight, List, Right} from "@element-plus/icons-vue";
import router from "@/router";

let userId = getUserId();
const formRef = ref()
const form1Ref = ref()
const form2Ref = ref()
const form3Ref = ref()
const step = ref(1)
const repairOrder = ref(0)
const formLabelItemWidth = '120px'

import {getUserIdentity} from "@/views/scripts/UserUtils";
import {getIdentityList} from "@/net";
let userGroup = ref("");
const identityList = ref([]);
const filtersList = ref([]);
const mainTableVisable = ref(false);
// 将从getIdentityList获得的json对象的数据部分存储为下拉选择框的数据数组
getIdentityList((data) => {
    identityList.value = data;
    // 将identityList转为filters的格式
    for (let i = 0; i < identityList.value.length; i++) {
        filtersList.value.push({
            text: identityList.value[i].label,
            value: identityList.value[i].value
        })
    }
    const userIdentity = getUserIdentity();
    userGroup.value = identityList.value.find((item) => {
        return item.value === userIdentity;
    }).label;
    // 调用接口并在成功时更新变量
    if (userGroup.value === "学生") {
        getStuDetail(userId, (data) => {
            email.value = data.email;
            phone.value = data.phoneNum;
        }, (error) => {
            console.error("获取用户详情失败", error);
        });
    } else {
        getStaffDetail(userId, (data) => {
            email.value = data.email;
            phone.value = data.phoneNum;
        }, (error) => {
            console.error("获取用户详情失败", error);
        });
    }
    document.title = "提交报修" + " - HMS(" + userGroup.value + ")";
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const email = ref(null);
const phone = ref(null);

const form = reactive({
    reporter: userId,
    building: '',
    unit: '',
    room: '',
    type: '',
    description: '',
    requirement: '',
    email: email.value,
    phone: phone.value
})

const rulePage1 = {
    building: [
        {required: true, message: '请选择楼栋号', trigger: 'blur'}
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'blur'}
    ],
    room: [
        {required: true, message: '请输入宿舍号', trigger: 'blur'}
    ]
}

const rulePage2 = {
    type: [
        {required: true, message: '请选择报修类型', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '请输入报修描述', trigger: 'blur'}
    ],
    requirement: [
        {required: true, message: '请选择报修需求', trigger: 'blur'}
    ]
}

const rulePage3 = {
    email: [
        {required: true, message: '请输入电子邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱', trigger: ['blur', 'change']}
    ],
    phone: [
        {required: true, message: '请输入手机号码', trigger: 'blur'},
        {min: 11, max: 11, message: '请输入合法的手机号码', trigger: ['blur', 'change']}
    ]
}

function next(curStep) {
    switch (curStep) {
        case 1:
            form1Ref.value.validate((valid) => {
                if (valid) {
                    step.value++;
                } else {
                    ElMessage.warning("请填写完整表单内容")
                }
            })
            break;
        case 2:
            form2Ref.value.validate((valid) => {
                if (valid) {
                    step.value++;
                } else {
                    ElMessage.warning("请填写完整表单内容")
                }
            })
            break;
        case 3:
            form3Ref.value.validate((valid) => {
                if (valid) {
                    step.value++;
                } else {
                    ElMessage.warning("请填写完整表单内容")
                }
            })
            break;
    }
}

const formRule = {
    building: [
        {required: true, message: '请选择楼栋号', trigger: 'blur'}
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'blur'}
    ],
    room: [
        {required: true, message: '请输入宿舍号', trigger: 'blur'}
    ],
    type: [
        {required: true, message: '请选择报修类型', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '请输入报修描述', trigger: 'blur'}
    ],
    requirement: [
        {required: true, message: '请选择报修需求', trigger: 'blur'}
    ],
    email: [
        {required: true, message: '请输入电子邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱', trigger: ['blur', 'change']}
    ],
    phone: [
        {required: true, message: '请输入手机号码', trigger: 'blur'},
        {min: 11, max: 11, message: '请输入合法的手机号码', trigger: ['blur', 'change']}
    ]
}

function submitApply() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/repair/apply', {...form}, (data) => {
                console.log(data);
                ElMessage.success("提交成功！");
                step.value++;
                repairOrder.value = data.order;
            })
        } else {
            ElMessage.warning("请填写完整表单内容")
        }
    })
}

// 前往历史报修界面查看此次报修详情
function toReportInfo() {
    const to = "/maintenance/detail/" + repairOrder.value;
    router.push(to);
}
</script>

<template>
    <div v-if="step<=4" class="container">
        <div class="container">
            <div class="step-part">
                <div style="height: 300px">
                    <el-steps direction="vertical"
                              :active="step"
                              style="width: 220px;"
                    >
                        <el-step title="第 1 步" description="选择你的报修位置"/>
                        <el-step title="第 2 步" description="填写报修信息"/>
                        <el-step title="第 3 步" description="确认你的联系方式"/>
                        <el-step title="第 4 步" description="提交报修单"/>
                    </el-steps>
                </div>
            </div>
            <div class="form-part">
                <el-form :model="form" :rules="formRule" ref="formRef" >
                <!-- 第一步 -->
                <div v-if="step === 1">
                    <el-form :model="form" :rules="rulePage1" ref="form1Ref">
                        <!-- 头部提示信息 -->
                        <div class="form-part-header">
                            <div class="form-part-header-title">报修位置</div>
                            <div class="form-part-header-description">
                                首先，我们需要了解你的报修位置，以便能够及时响应你的报修请求。
                            </div>
                        </div>
                        <!-- 表单内容 -->
                        <div class="form-part-main">
                            <el-form-item label="楼栋号"
                                          :label-width="formLabelItemWidth" prop="building">
                                <el-select v-model="form.building"
                                           style="width: 300px;"
                                           placeholder="请选择楼栋号"
                                >
                                    <el-option label="01栋" value="01"/>
                                    <el-option label="02栋" value="02"/>
                                    <el-option label="03栋" value="03"/>
                                    <el-option label="04栋" value="04"/>
                                    <el-option label="05栋" value="05"/>
                                    <el-option label="06栋" value="06"/>
                                    <el-option label="07栋" value="07"/>
                                    <el-option label="08栋" value="08"/>
                                    <el-option label="09栋" value="09"/>
                                    <el-option label="10栋" value="10"/>
                                    <el-option label="11栋" value="11"/>
                                    <el-option label="12栋" value="12"/>
                                    <el-option label="13栋" value="13"/>
                                    <el-option label="14栋" value="14"/>
                                    <el-option label="15栋" value="15"/>
                                    <el-option label="16栋" value="16"/>
                                    <el-option label="17栋" value="17"/>
                                    <el-option label="18栋" value="18"/>
                                    <el-option label="19栋" value="19"/>
                                    <el-option label="20栋" value="20"/>
                                    <el-option label="21栋" value="21"/>
                                    <el-option label="22栋" value="22"/>
                                    <el-option label="23栋" value="23"/>
                                    <el-option label="24栋" value="24"/>
                                    <el-option label="25栋" value="25"/>
                                    <el-option label="26栋" value="26"/>
                                    <el-option label="27栋" value="27"/>
                                    <el-option label="28栋" value="28"/>
                                    <el-option label="29栋" value="29"/>
                                    <el-option label="30栋" value="30"/>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="单元号" :label-width="formLabelItemWidth" prop="unit">
                                <el-select v-model="form.unit"
                                           style="width: 300px;"
                                           placeholder="请选择单元号">
                                    <el-option label="A" value="A"/>
                                    <el-option label="B" value="B"/>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="宿舍号" :label-width="formLabelItemWidth" prop="room">
                                <el-input v-model="form.room"
                                          placeholder="请输入宿舍号"
                                          maxlength="3"
                                          style="width: 300px;"
                                />
                            </el-form-item>
                        </div>
                    </el-form>
                    <div class="form-part-footer">
                        <el-button type="primary" :icon="DArrowLeft" @click="step--" disabled>上一步</el-button>
                        <el-button type="primary" @click="next(step)">
                            下一步
                            <el-icon class="el-icon--right">
                                <DArrowRight/>
                            </el-icon>
                        </el-button>
                    </div>
                </div>
                <!-- 第二步 -->
                <div v-if="step === 2">
                    <el-form :model="form" :rules="rulePage2" ref="form2Ref">
                        <!-- 头部提示信息 -->
                        <div class="form-part-header">
                            <div class="form-part-header-title">报修信息</div>
                            <div class="form-part-header-description">
                                然后，请填写你的报修详情，以便我们能够更快提供维修服务。
                            </div>
                        </div>
                        <!-- 表单内容 -->
                        <div class="form-part-main">
                            <el-form-item label="报修类型"
                                          :label-width="formLabelItemWidth" prop="type">
                                <el-select v-model="form.type"
                                           style="width: 300px;"
                                           placeholder="请选择报修类型"
                                >
                                    <el-option label="网络" value="网络"/>
                                    <el-option label="电器" value="电器"/>
                                    <el-option label="家具" value="家具"/>
                                    <el-option label="其他" value="其他"/>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="报修描述"
                                          :label-width="formLabelItemWidth"
                                          prop="description">
                                <el-input v-model="form.description"
                                          type="textarea"
                                          placeholder="请输入报修描述"
                                          style="width: 500px;"
                                />
                            </el-form-item>
                            <el-form-item label="报修需求"
                                          :label-width="formLabelItemWidth"
                                          prop="requirement">
                                <!-- 维修、更换等 -->
                                <el-radio-group v-model="form.requirement"
                                                style="width: 300px">
                                    <el-radio-button label="维修">维修</el-radio-button>
                                    <el-radio-button label="更换">更换</el-radio-button>
                                    <el-radio-button label="检查">检查</el-radio-button>
                                    <el-radio-button label="其他">其他</el-radio-button>
                                </el-radio-group>
                            </el-form-item>
                        </div>
                    </el-form>
                    <div class="form-part-footer">
                        <el-button type="primary" :icon="DArrowLeft" @click="step--">上一步</el-button>
                        <el-button type="primary" @click="next(step)">
                            下一步
                            <el-icon class="el-icon--right">
                                <DArrowRight/>
                            </el-icon>
                        </el-button>
                    </div>
                </div>
                <!-- 第三步 -->
                <div v-if="step === 3">
                    <el-form :model="form" :rules="rulePage3" ref="form3Ref">
                        <!-- 头部提示信息 -->
                        <div class="form-part-header">
                            <div class="form-part-header-title">联系方式</div>
                            <div class="form-part-header-description">
                                接下来，请确认你的联系方式，以便我们能够及时与你沟通。
                            </div>
                        </div>
                        <!-- 表单内容 -->
                        <div class="form-part-main">
                            <el-form-item label="电子邮箱"
                                          :label-width="formLabelItemWidth"
                                          prop="email">
                                <el-input v-model="form.email"
                                          type="email"
                                          placeholder="请输入电子邮箱"
                                          style="width: 300px;"
                                />
                            </el-form-item>
                            <el-form-item label="手机号码"
                                          :label-width="formLabelItemWidth"
                                          prop="phone">
                                <el-input v-model="form.phone"
                                          placeholder="请输入手机号码"
                                          style="width: 300px;"
                                />
                            </el-form-item>
                        </div>
                        <div class="form-part-footer">
                            <el-button type="primary" :icon="DArrowLeft" @click="step--">上一步</el-button>
                            <el-button type="primary" @click="next(step)">
                                下一步
                                <el-icon class="el-icon--right">
                                    <DArrowRight/>
                                </el-icon>
                            </el-button>
                        </div>
                    </el-form>
                </div>
                <!-- 第四步，确认信息。将之前填写的内容显示出来，然后提交 -->
                <div v-if="step===4">
                    <!-- 头部提示信息 -->
                    <div class="form-part-header">
                        <div class="form-part-header-title">确认信息</div>
                        <div class="form-part-header-description">
                            最后，请确认填写无误，然后提交报修单。
                        </div>
                    </div>
                    <!-- 表单内容，以表格形式列出 -->
                    <div class="form-part-main">
                        <el-table
                            :data="[
                                {name: '报修位置', value: `${form.building}栋${form.unit}单元${form.room}室`},
                                {name: '报修类型', value: form.type},
                                {name: '报修描述', value: form.description},
                                {name: '报修需求', value: form.requirement},
                                {name: '电子邮箱', value: form.email},
                                {name: '手机号码', value: form.phone}
                                ]"
                            style="max-width: 80%;
                                   position: relative;
                                   left: 0;
                                   right: 0;
                                   margin: 0 auto;"
                        >
                            <el-table-column prop="name" label="项目" width="150"></el-table-column>
                            <el-table-column prop="value" label="内容"></el-table-column>
                        </el-table>
                    </div>
                    <div class="form-part-footer">
                        <el-button type="primary" :icon="DArrowLeft" @click="step--">上一步</el-button>
                        <el-button type="success" @click="submitApply">
                            提交报修单
                            <el-icon class="el-icon--right">
                                <CircleCheck/>
                            </el-icon>
                        </el-button>
                    </div>
                </div>
                                </el-form>
            </div>
        </div>
    </div>
    <!-- 完成填写，即step===5 -->
    <div v-if="step===5" class="container">
        <!-- 提示已经成功提交，提供查看报修历史的链接 -->
        <div class="container">
            <div class="form-part-header">
                <div class="form-part-header-title">提交成功</div>
                <div class="form-part-header-description">
                    你的报修单已经成功提交，我们将尽快为你提供维修服务。
                </div>
                <div style="margin-top: 50px">
                    <el-button type="primary" :icon="List" @click="toReportInfo">
                        返回报修单
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import "./style/apply.css";
</style>