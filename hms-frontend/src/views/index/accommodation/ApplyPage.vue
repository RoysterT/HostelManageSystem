<script setup>
import {getUserGroup, getUserId} from "@/views/scripts/UserUtils";
import {reactive, ref} from "vue";
import {post, get} from "@/net";
import {ElMessage} from "element-plus";
import {Checked, CircleCloseFilled, List, QuestionFilled, Refresh, SuccessFilled, Tools} from "@element-plus/icons-vue";
import {copy} from "@/views/scripts/ManageUtils";

let userId = getUserId();
const formRef = ref()
const loading = ref(false)
const formLabelItemWidth = '120px'
const hasLatestApply = ref(false)
const latestApplyData = reactive({
    studentIdA: '',
    studentIdB: '',
    description: '',
    creationTime: '',
    progress: 0,
    finishTime: '',
})

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
    document.title = "提交换宿申请" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    // updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const form = reactive({
    studentIdA: userId,
    studentIdB: '',
    description: ''
})

const rule = {
    studentIdB: [
        {required: true, message: '请输入对方学号', trigger: 'blur'},
        {min: 9, max: 9, message: '学号长度不正确', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '请输入换宿原因', trigger: 'blur'},
        {min: 10, message: '请输入至少10个字符', trigger: 'blur'}
    ]
}

function submitApply() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/dorm/apply/new', {...form}, (data) => {
                console.log(data);
                ElMessage.success("提交成功！");
                // 刷新页面
                window.location.reload();
            })
        } else {
            ElMessage.warning("请填写完整表单内容")
        }
    })
}

function reloadPage() {
    loading.value = true;
    get('/api/dorm/apply/info/id/' + getUserId(), (data) => {
        // 等待随机时间后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            if (data == null) {
                hasLatestApply.value = false;
                loading.value = false;
                return;
            }
            console.log(data)
            latestApplyData.studentIdA = data.studentIdA;
            latestApplyData.studentIdB = data.studentIdB;
            latestApplyData.description = data.description;
            latestApplyData.creationTime = data.creationTime;
            latestApplyData.progress = data.progress;
            latestApplyData.finishTime = data.finishTime;
            hasLatestApply.value = true;
            loading.value = false;
            ElMessage.success("数据获取成功！");
        }, random);
    }, (error) => {
        console.error("数据获取失败", error);
        loading.value = false;
    });
}

reloadPage();

</script>

<template>
    <!-- 默认显示这一部分，除非查询到了近期申请记录 -->
    <div v-if="!hasLatestApply" style="display: flex; align-items: center;flex-direction: column">
        <!-- 标题栏 -->
        <el-row style="margin-top: 80px">
            <el-col :span="24">
                <h1>提交换宿申请</h1>
            </el-col>
        </el-row>
        <el-form :model="form" :rules="rule" ref="formRef" label-width="120px"
                 style="width: 600px">
            <el-form-item label="学号" prop="studentIdA">
                <el-input v-model="form.studentIdA" disabled></el-input>
            </el-form-item>
            <el-form-item label="对方学号" prop="studentIdB">
                <el-input v-model="form.studentIdB"></el-input>
            </el-form-item>
            <el-form-item label="换宿原因" prop="description">
                <el-input type="textarea" v-model="form.description"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submitApply">提交申请</el-button>
            </el-form-item>
        </el-form>
    </div>
    <!-- 否则将latestApplyData的数据用el-description显示出来 -->
    <div v-else style="display: flex; flex-direction: column; align-items: center">
        <el-descriptions
            class="margin-top"
            title="换宿详情"
            :column="1"
            :size="'large'"
            style="width: 880px;padding-top: 80px"
            border
        >
            <template #extra>
                <el-button type="warning" :icon="Refresh"
                           v-loading="loading" @click="reloadPage">刷新数据
                </el-button>
                <!-- 提交新申请 -->
                <el-button type="primary" :icon="QuestionFilled" v-if="latestApplyData.progress >= 5"
                           @click="hasLatestApply = !hasLatestApply">提交新申请
                </el-button>
                <el-button type="primary" :icon="QuestionFilled" v-else disabled title="请先完成此次申请"
                           @click="hasLatestApply = !hasLatestApply">提交新申请
                </el-button>
            </template>

            <!-- 使用 el-description 将返回数据显示出来 -->
            <el-descriptions-item label="交换学生学号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(latestApplyData.studentIdA)">
                            {{ latestApplyData.studentIdA }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="被交换学生学号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(latestApplyData.studentIdB)">
                            {{ latestApplyData.studentIdB }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="换宿原因">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(latestApplyData.description)">
                            {{ latestApplyData.description }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(latestApplyData.createTime)">
                            {{ latestApplyData.creationTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(latestApplyData.finishTime)">
                            {{ latestApplyData.finishTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="换宿进度">
                <template #default>
                    <el-steps :active="latestApplyData.progress<5?latestApplyData.progress-1:5"
                              finish-status="success"
                              align-center
                              v-if="latestApplyData.progress<=5"
                    >
                        <el-step title="提交申请" :icon="List"></el-step>
                        <el-step title="楼栋管理员A审核" :icon="Checked"></el-step>
                        <el-step title="楼栋管理员B审核" :icon="Checked"></el-step>
                        <el-step title="学校审核" :icon="QuestionFilled"></el-step>
                        <el-step title="申请完成" :icon="SuccessFilled"></el-step>
                    </el-steps>
                    <el-steps :active="1"
                              finish-status="success"
                              align-center
                              process-status="error"
                              v-else-if="latestApplyData.progress==6"
                    >
                        <el-step title="提交申请" :icon="List"></el-step>
                        <el-step title="楼栋管理员审核" :icon="CircleCloseFilled"></el-step>
                        <el-step title="学校审核" :icon="Checked"></el-step>
                    </el-steps>
                    <el-steps :active="3"
                              finish-status="success"
                              align-center
                              process-status="error"
                              v-else-if="latestApplyData.progress==7"
                    >
                        <el-step title="提交申请" :icon="List"></el-step>
                        <el-step title="楼栋管理员A审核" :icon="Checked"></el-step>
                        <el-step title="楼栋管理员B审核" :icon="Checked"></el-step>
                        <el-step title="学校审核" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                </template>
            </el-descriptions-item>
        </el-descriptions>
    </div>
</template>

<style scoped>

</style>