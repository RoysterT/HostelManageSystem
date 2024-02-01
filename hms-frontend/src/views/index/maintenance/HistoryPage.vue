<script lang="ts" setup>
import {reactive, ref, watch} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {get, getUserAccommodation, getRepairInfoByOrder, confirmFinishRepair} from "@/net";
import {getUserId} from "@/views/scripts/UserUtils";
import {copy} from "@/views/scripts/ManageUtils";
import {ElMessage, ElTable} from "element-plus";
import {
    Avatar,
    Checked, CircleCheck,
    CircleCloseFilled,
    Delete,
    Edit,
    InfoFilled,
    List, QuestionFilled, Refresh, Search,
    SuccessFilled,
    Tools
} from "@element-plus/icons-vue";

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const loading = ref(false)
const getDataFlag = ref(false)
let userId = getUserId();
const building = ref(null);
const unit = ref(null);
const room = ref(null);
let infoDrawerVisible = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const pageSizes = ref([1, 2, 5, 10, 20, 30, 50, 100]);
const background = ref(true);
const small = ref(false);

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
    document.title = "宿舍往期报修" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

// 监听pageSize变化，更新表格
const handleSizeChange = (val: number) => {
    console.log(`${val} items per page`)
    pageSize.value = val;
    updateInfoTable();
}

// 监听currentPage变化，更新表格
const handleCurrentChange = (val: number) => {
    console.log(`current page: ${val}`)
    currentPage.value = val;
    updateInfoTable();
}

const toggleSelection = (rows?: Repair[]) => {
    if (rows) {
        rows.forEach((row) => {
            // TODO: improvement typing when refactor table
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-expect-error
            multipleTableRef.value!.toggleRowSelection(row, undefined)
        })
    } else {
        multipleTableRef.value!.clearSelection()
    }
}

interface Repair {
    repairOrder: number,
    reporter: string,
    building: string,
    unit: string,
    room: string,
    type: string,
    description: string,
    requirement: string,
    email: string,
    phone: string,
    creationTime: string,
    finishTime: string,
    worker: string,
    dormManager: string,
    progress: number
}

getUserAccommodation(userId, (data) => {
    building.value = data.building;
    unit.value = data.unit;
    room.value = data.room;
    getDataFlag.value = true;
}, (error) => {
    console.error("获取住宿信息失败", error);
});

function updateInfoTable() {
    loading.value = true;
    const url = '/api/repair/history?page=' + currentPage.value
        + '&pageSize=' + pageSize.value
        + '&building=' + building.value
        + '&unit=' + unit.value
        + '&room=' + room.value;
    get(url, (data) => {
        console.log("获取数据成功", data);
        // 等待随机时间后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            // 将每一组数据的创建时间和完成时间转换为当前时区的时间字符串，如果为空则不转换
            data.data.forEach((item: Repair) => {
                if (item.creationTime != null) {
                    item.creationTime = new Date(item.creationTime).toLocaleString();
                }
                if (item.finishTime != null) {
                    item.finishTime = new Date(item.finishTime).toLocaleString();
                }
            })
            tableData.value = data.data;
            total.value = data.total;
            loading.value = false;
        }, random);
    }, (error) => {
        console.error("数据获取失败", error);
        loading.value = false;
    });
}

// 监听 room 变量的变化，如果变化则重新获取数据
watch(room, () => {
    updateInfoTable();
});

const repairInfoDrawerData = reactive({
    repairOrder: null,
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
    progress: null
})

// 查看报修详情
function repairInfo(row: Repair) {
    // 在当前标签页访问/maintenance/detail/:repairOrder
    window.open("/maintenance/detail/" + row.repairOrder, "_self");
}
/*function repairInfo(row: Repair) {
    // 从接口getRepairInfoByOrder得到的json的data中获取选择的用户组权限信息
    getRepairInfoByOrder(row.repairOrder, (data) => {
        console.log("data:", data);
        repairInfoDrawerData.repairOrder = data.repairOrder;
        repairInfoDrawerData.reporter = data.reporter;
        repairInfoDrawerData.room = data.building + "栋" + data.unit + "单元" + data.room + "房";
        repairInfoDrawerData.type = data.type;
        repairInfoDrawerData.description = data.description;
        repairInfoDrawerData.requirement = data.requirement;
        repairInfoDrawerData.email = data.email;
        repairInfoDrawerData.phone = data.phone;
        // 把时间戳转换为当前时区的时间字符串，如果为空则不转换
        if (data.creationTime != null) {
            repairInfoDrawerData.creationTime = new Date(data.creationTime).toLocaleString();
        }
        if (data.finishTime != null) {
            repairInfoDrawerData.finishTime = new Date(data.finishTime).toLocaleString();
        }
        repairInfoDrawerData.worker = data.worker;
        repairInfoDrawerData.dormManager = data.dormManager;
        repairInfoDrawerData.progress = data.progress;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取报修详情失败", error);
    });
}*/

// 在详情页面复制整单报修信息
function copyAllInfo() {
    // 将报修进度更改为对应步骤的文字
    const progressText = ["住户提交报修", "楼栋管理员进行确认", "正在维修",
        "维修完成，等待确认", "已完成", "报修单已被楼栋管理员关闭", "报修单已被维修人员关闭"]
    copy("报修单号【" + repairInfoDrawerData.repairOrder + "】的详细信息：" +
        "报修人id为【" + repairInfoDrawerData.reporter + "】，" +
        "报修位置是【" + repairInfoDrawerData.room + "】，" +
        "类型是【" + repairInfoDrawerData.type + "】，" +
        "描述为：" + repairInfoDrawerData.description + "\n" +
        "要求是【" + repairInfoDrawerData.requirement + "】。" +
        "该报修人的联系邮箱：" + repairInfoDrawerData.email + "，" +
        "手机号：" + repairInfoDrawerData.phone + "。" +
        "提交报修的时间是" + repairInfoDrawerData.creationTime + "，" +
        "该报修订单完成时间：【" + (repairInfoDrawerData.finishTime || "未完成") + "】，" +
        "维修负责人是【" + (repairInfoDrawerData.worker || "未分配") + "】，" +
        "楼栋负责人是【" + (repairInfoDrawerData.dormManager || "未分配") + "】，" +
        "当前维修进度：" + progressText[repairInfoDrawerData.progress] + "\n"
    )
}

function confirmSuccessRepair(row: Repair){
    console.log("确认完成报修单", row.repairOrder);
    confirmFinishRepair(row.repairOrder, (data) => {
        console.log("确认完成报修单成功", data);
        ElMessage.success("确认完成报修单成功")
        updateInfoTable();
    }, (error) => {
        console.error("确认完成报修单失败", error);
    });
}
</script>

<template>
    <div class="table-container">
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
            </div>
        </div>
        <div v-if="getDataFlag" class="table-container">
            <el-table :data="tableData"
                      class-name="table-main"
                      empty-text="未查询到报修历史！"
                      header-align="center"
                      ref="multipleTableRef"
                      v-loading="loading"
                      v-if="mainTableVisable"
                      style="width: 100%; overflow: scroll"
            >
                <el-table-column type="selection" width="80" fixed align="center"/>
                <el-table-column prop="repairOrder" label="报修单号" min-width="140" sortable align="center"/>
                <el-table-column prop="type" label="报修类型" min-width="140" sortable align="center"/>
                <el-table-column prop="description" label="报修描述" min-width="260" sortable align="center"/>
                <el-table-column prop="requirement" label="报修要求" min-width="140" sortable align="center"/>
                <el-table-column prop="creationTime" label="报修时间" min-width="220" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy(row.creationTime)">{{ row.creationTime }}</el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="progress" label="维修进度" min-width="180" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy('等待楼栋管理员进行确认')"
                                       v-if="row.progress==2">等待楼栋管理员进行确认</el-button>
                            <el-button type="text" @click="copy('正在分配维修人员')"
                                       v-else-if="row.progress==3">正在维修</el-button>
                            <el-button type="text" @click="copy('正在维修')"
                                       v-else-if="row.progress==4">维修完成，等待确认</el-button>
                            <el-button type="text" @click="copy('维修完成，等待确认')"
                                       v-else-if="row.progress==5">维修结束</el-button>
                            <el-button type="text" @click="copy('报修单已被楼栋管理员关闭')"
                                        v-else-if="row.progress==6">报修单已被楼栋管理员关闭</el-button>
                            <el-button type="text" @click="copy('报修单已被维修人员关闭')"
                                        v-else-if="row.progress==7">报修单已被维修人员关闭</el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <!-- 操作列 -->
                <el-table-column prop="repairOrder" label="操作" fixed="right" width="200" align="center">
                    <template #default="{row}">
                        <el-button type="success" size="small" :icon="CircleCheck"
                                   @click="confirmSuccessRepair(row)" v-if="row.progress==4">确认完成</el-button>
                        <el-button type="success" size="small" :icon="CircleCheck" title="该报修单已经确认"
                                   @click="confirmSuccessRepair(row)" v-else-if="row.progress==5" disabled>已确认</el-button>
                        <el-button type="success" size="small" :icon="CircleCheck" title="该报修单无法确认"
                                   @click="confirmSuccessRepair(row)" v-else disabled>确认完成</el-button>
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="repairInfo(row)">详情</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="demo-pagination-block table-footer">
                <el-pagination
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="pageSizes"
                    :small="small"
                    :background="background"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                />
            </div>
        </div>
    </div>
    <!-- 报修详情抽屉 -->
    <el-drawer v-model="infoDrawerVisible"
               title="报修详情"
               :direction="'rtl'"
               :with-header="true"
               size="30%"
    >
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">报修单号[ {{ repairInfoDrawerData.repairOrder }} ]的详情信息</h4>
        </template>
        <!-- 使用描述列表el-descriptions显示内容，字段全部可以可以一键复制 -->
        <el-descriptions :column="1" border>
            <el-descriptions-item label="单号">{{ repairInfoDrawerData.repairOrder }}</el-descriptions-item>
            <el-descriptions-item label="报修人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.reporter)">
                            {{ repairInfoDrawerData.reporter }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="房间号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.room)">{{
                                repairInfoDrawerData.room
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修类型">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.type)">{{
                                repairInfoDrawerData.type
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修描述">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.description)">
                            {{ repairInfoDrawerData.description }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修要求">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.requirement)">
                            {{ repairInfoDrawerData.requirement }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修人邮箱">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.email)">{{
                                repairInfoDrawerData.email
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修人手机号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.phone)">{{
                                repairInfoDrawerData.phone
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="报修时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.creationTime)">
                            {{ repairInfoDrawerData.creationTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.finishTime)">
                            {{ repairInfoDrawerData.finishTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="维修负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.worker)">{{
                                repairInfoDrawerData.worker
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="楼栋负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(repairInfoDrawerData.dormManager)">
                            {{ repairInfoDrawerData.dormManager }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="维修进度">
                <template #default>
                    <el-steps :active="repairInfoDrawerData.progress-1"
                              finish-status="success"
                              align-center
                              direction="vertical"
                              v-if="repairInfoDrawerData.progress<=5"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="正在维修" :icon="Tools"></el-step>
                        <el-step title="维修完成，等待确认" :icon="QuestionFilled"></el-step>
                        <el-step title="已完成" :icon="SuccessFilled"></el-step>
                    </el-steps>
                    <el-steps :active="repairInfoDrawerData.progress-5"
                              finish-status="success"
                              align-center
                              process-status="error"
                              direction="vertical"
                              v-else-if="repairInfoDrawerData.progress==6"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="报修单已被楼栋管理员关闭" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                    <el-steps :active="repairInfoDrawerData.progress-5"
                              finish-status="success"
                              align-center
                              process-status="error"
                              direction="vertical"
                              v-else-if="repairInfoDrawerData.progress==7"
                    >
                        <el-step title="住户提交报修" :icon="List"></el-step>
                        <el-step title="楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="报修单已被维修人员关闭" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                </template>
            </el-descriptions-item>
        </el-descriptions>
        <!-- 底部确认按钮 -->
        <template #footer>
            <span class="dialog-footer">
                <el-button type="primary" @click="copyAllInfo">复制整单报修信息</el-button>
                <el-button @click="infoDrawerVisible = false">完成</el-button>
            </span>
        </template>
    </el-drawer>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>