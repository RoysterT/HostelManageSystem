<script setup>
import {reactive, ref} from 'vue';
import {getUserGroup} from "@/views/scripts/UserUtils";
import {confirmFinishRepair, getRepairDetail} from "@/net";
import {copy} from "@/views/scripts/ManageUtils";
import {
    Checked,
    CircleCloseFilled,
    List,
    QuestionFilled,
    Refresh,
    Search,
    SuccessFilled,
    Tools
} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";

const data = ref([]);
let repairOrder = ref(0);
let userGroup = getUserGroup();
const checkFlag = ref(false);
const loading = ref(false)
document.title = "报修详情页" + " - HMS(" + userGroup + ")";

const repairInfoData = reactive({
    repairOrder: 0,
    reporter: '',
    building: '',
    unit: '',
    room: '',
    type: '',
    description: '',
    requirement: '',
    email: '',
    phone: '',
    creationTime: '',
    finishTime: '',
    worker: '',
    dormManager: '',
    progress: 0
})

function updateInfoTable() {
    // 获取报修单号
    repairOrder.value = document.getElementById("repairOrder").innerText;
    loading.value = true;
    // 更新报修信息
    getRepairDetail(repairOrder.value, (res) => {
        const random = 500;
        setTimeout(() => {
            repairInfoData.repairOrder = res.repairOrder;
            repairInfoData.reporter = res.reporter;
            repairInfoData.building = res.building;
            repairInfoData.unit = res.unit;
            repairInfoData.room = res.room;
            repairInfoData.type = res.type;
            repairInfoData.description = res.description;
            repairInfoData.requirement = res.requirement;
            repairInfoData.email = res.email;
            repairInfoData.phone = res.phone;
            repairInfoData.creationTime = res.creationTime ? new Date(res.creationTime).toLocaleString() : "";
            repairInfoData.finishTime = res.finishTime ? new Date(res.finishTime).toLocaleString() : "";
            repairInfoData.worker = res.worker;
            repairInfoData.dormManager = res.dormManager;
            repairInfoData.progress = res.progress;
            checkFlag.value = true;
            loading.value = false;
            ElMessage.success("刷新成功！")
        }, random);
    }, (err) => {
        console.log(err);
    })
}

window.onload = function () {
    setTimeout(() => {
        updateInfoTable();
    }, 500);
}

// 在详情页面复制整单报修信息
function copyAllInfo() {
    // 将报修进度更改为对应步骤的文字
    const progressText = ["住户提交报修", "楼栋管理员进行确认", "正在维修",
        "维修完成，等待确认", "已完成", "报修单已被楼栋管理员关闭", "报修单已被维修人员关闭"]
    copy("报修单号【" + repairInfoData.repairOrder + "】的详细信息：" +
        "报修人id为【" + repairInfoData.reporter + "】，" +
        "报修位置是【" + repairInfoData.room + "】，" +
        "类型是【" + repairInfoData.type + "】，" +
        "描述为：" + repairInfoData.description + "\n" +
        "要求是【" + repairInfoData.requirement + "】。" +
        "该报修人的联系邮箱：" + repairInfoData.email + "，" +
        "手机号：" + repairInfoData.phone + "。" +
        "提交报修的时间是" + repairInfoData.creationTime + "，" +
        "该报修订单完成时间：【" + (repairInfoData.finishTime || "未完成") + "】，" +
        "维修负责人是【" + (repairInfoData.worker || "未分配") + "】，" +
        "楼栋负责人是【" + (repairInfoData.dormManager || "未分配") + "】，" +
        "当前维修进度：" + progressText[repairInfoData.progress] + "\n"
    )
}

function confirmSuccessRepair(repairOrder) {
    console.log("确认完成报修单", repairOrder);
    confirmFinishRepair(repairOrder, (data) => {
        console.log("确认完成报修单成功", data);
        ElMessage.success("确认完成报修单成功")
        updateInfoTable();
    }, (error) => {
        console.error("确认完成报修单失败", error);
    });
}

</script>

<template>
    <span id="repairOrder" style="display: none">{{ $route.params.repairOrder }}</span>
    <div class="table-container">
        <!-- 顶部功能区：刷新、复制报修内容 -->
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <!-- 显示维修单号 -->
                <el-button type="primary" :icon="Search" @click="copyAllInfo">复制报修内容</el-button>
                <el-button type="warning" :icon="Refresh"
                           v-loading="loading" @click="updateInfoTable">刷新数据
                </el-button>
                <!-- 确认完成 -->
                <el-button type="success"
                           :icon="SuccessFilled"
                           v-if="repairInfoData.progress===4"
                           @click="confirmSuccessRepair(repairInfoData.repairOrder)">确认完成
                </el-button>
                <el-button type="success"
                           :icon="SuccessFilled"
                           v-else
                           @click="confirmSuccessRepair(repairInfoData.repairOrder)"
                           disabled>确认完成
                </el-button>
            </div>
        </div>
        <!-- 使用 el-description 将返回数据显示出来 -->
        <el-descriptions :column="2" style="min-width: 900px; height: 90%" border v-if="checkFlag" v-loading>
            <el-descriptions-item label="单号">{{ repairInfoData.repairOrder }}</el-descriptions-item>
            <el-descriptions-item label="报修人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.reporter)">
                            {{ repairInfoData.reporter }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="房间号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.room)">{{
                                repairInfoData.room
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修类型">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.type)">{{
                                repairInfoData.type
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修描述">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.description)">
                            {{ repairInfoData.description }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修要求">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.requirement)">
                            {{ repairInfoData.requirement }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修人邮箱">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.email)">{{
                                repairInfoData.email
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修人手机号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.phone)">{{
                                repairInfoData.phone
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.creationTime)">
                            {{ repairInfoData.creationTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.finishTime)">
                            {{ repairInfoData.finishTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="维修负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.worker)">{{
                                repairInfoData.worker || "未分配"
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="楼栋负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoData.dormManager)">
                            {{ repairInfoData.dormManager || "未分配" }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="维修进度">
                <template #default>
                    <el-steps :active="repairInfoData.progress-1"
                              finish-status="success"
                              align-center
                              v-if="repairInfoData.progress<=5"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="正在维修" :icon="Tools"></el-step>
                        <el-step title="维修完成，等待确认" :icon="QuestionFilled"></el-step>
                        <el-step title="已完成" :icon="SuccessFilled"></el-step>
                    </el-steps>
                    <el-steps :active="repairInfoData.progress-5"
                              finish-status="success"
                              align-center
                              process-status="error"
                              v-else-if="repairInfoData.progress===6"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="报修单已被楼栋管理员关闭" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                    <el-steps :active="repairInfoData.progress-5"
                              finish-status="success"
                              align-center
                              process-status="error"
                              v-else-if="repairInfoData.progress===7"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="报修单已被维修人员关闭" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                </template>
            </el-descriptions-item>
        </el-descriptions>
    </div>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>
