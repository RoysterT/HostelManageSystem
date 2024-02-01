<script lang="ts" setup>
import {reactive, ref, watch} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {get, getStaffDetail, getPaymentInfo, confirmPayment} from "@/net";
import {getUserId} from "@/views/scripts/UserUtils";
import {copy} from "@/views/scripts/ManageUtils";
import {ElMessage, ElMessageBox, ElTable} from "element-plus";
import {
    Avatar,
    Checked,
    CircleCloseFilled,
    Delete,
    Edit,
    InfoFilled,
    List, Money, QuestionFilled, Refresh, Search,
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
const manageBuilding = ref(null);
let infoDrawerVisible = ref(false)
const isToConfirm = ref(true);
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
    document.title = "楼栋缴费管理" + " - HMS(" + userGroup.value + ")";
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

const toggleSelection = (rows?: Payment[]) => {
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

interface Payment {
    paymentOrder: number,
    paymentId: string,
    building: string,
    unit: string,
    room: string,
    creationTime: number,
    amount: number,
    description: string,
    paymentTime: number,
    payer: string,
    paymentMethod: string,
    progress: number
}

// 监听currentPage变化，更新表格
const handleCurrentChange = (val: number) => {
    console.log(`current page: ${val}`)
    currentPage.value = val;
    updateInfoTable();
}

getStaffDetail(userId, (data) => {
    manageBuilding.value = data.manageBuilding;
    getDataFlag.value = true;
}, (error) => {
    console.error("获取管理楼栋信息失败", error);
});

// 将 getPaymentConfirm 封装为一个函数，方便调用
function updateInfoTable() {
    if (manageBuilding.value == null || manageBuilding.value == "") {
        console.log("manageBuilding 为空，不进行查询");
        return;
    }
    loading.value = true;
    let type = isToConfirm.value ? "confirm" : "all";
    const url = '/api/payment/list/building/'
        + manageBuilding.value
        + '/' + type
        + '?page=' + currentPage.value
        + '&pageSize=' + pageSize.value;
    get(url, (data) => {
        // 等待随机时间后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            tableData.value = data.data;
            total.value = data.total;
            loading.value = false;
        }, random);
    }, (error) => {
        console.error("数据获取失败", error);
        loading.value = false;
    });
}

// 监听 manageBuilding 变量的变化，如果变化则重新获取数据
watch(manageBuilding, () => {
    updateInfoTable();
});

const paymentInfoDrawerData = reactive({
    paymentOrder: 0,
    paymentId: '',
    room: '',
    creationTime: '',
    amount: 0,
    description: '',
    paymentTime: '',
    payer: '',
    paymentMethod: '',
    progress: 0
})

// 查看缴费单详情
function paymentInfo(row: Payment) {
    // 从接口getPaymentInfoByOrder得到的json的data中获取选择的用户组权限信息
    getPaymentInfo(row.paymentId, (data) => {
        paymentInfoDrawerData.paymentOrder = data.paymentOrder;
        paymentInfoDrawerData.paymentId = data.paymentId;
        paymentInfoDrawerData.room = data.building + "栋" + data.unit + "单元" + data.room + "室";
        paymentInfoDrawerData.creationTime = new Date(data.creationTime).toLocaleString();
        paymentInfoDrawerData.amount = data.amount;
        paymentInfoDrawerData.description = data.description;
        if (data.paymentTime != null) {
            paymentInfoDrawerData.paymentTime = new Date(data.paymentTime).toLocaleString();
        } else {
            paymentInfoDrawerData.paymentTime = "未缴费";
        }
        paymentInfoDrawerData.payer = data.payer;
        paymentInfoDrawerData.paymentMethod = data.paymentMethod;
        paymentInfoDrawerData.progress = data.progress;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取缴费单详情失败", error);
    });
}

// 创建一个对话框函数，将传入字段显示在内容中，并返回选择的boolean
const createDialog = (title: string, content: string) => {
    return new Promise((resolve, reject) => {
        ElMessageBox.confirm(content, title, {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            resolve(true)
        }).catch(() => {
            resolve(false)
        })
    })
}

function paymentConfirm(row: Payment) {
    confirmPayment(row.paymentId, (data) => {
        console.log("确认成功", data);
        ElMessage.success("确认成功");
        updateInfoTable();
    }, (error) => {
        console.error("提交确认失败", error);
        ElMessage.error("提交确认失败");
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
                <!-- 切换视图按钮，改变isToConfirm的值，即“切换到全部记录”和”切换到待确认记录“ -->
                <el-button type="primary" size="small" :icon="Refresh"
                           @click="isToConfirm = !isToConfirm; updateInfoTable()">
                    <template v-if="isToConfirm">查看全部记录</template>
                    <template v-else>仅查看待确认记录</template>
                </el-button>
            </div>
        </div>
        <div class="table-container">
            <el-table :data="tableData"
                      class-name="table-main"
                      empty-text="未查询到数据！"
                      header-align="center"
                      ref="multipleTableRef"
                      v-loading="loading"
                      v-if="getDataFlag && mainTableVisable"
                      style="width: 100%; overflow: scroll"
            >
                <el-table-column type="selection" width="80" fixed align="center"/>
                <el-table-column prop="paymentOrder" label="记录序号" min-width="120" sortable align="center"/>
                <el-table-column prop="paymentId" label="缴费单号" min-width="200" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text"
                                       @click="copy(row.paymentId)">
                                {{ row.paymentId }}
                            </el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="progress" label="缴费进度" min-width="220" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy('核实金额')"
                                       v-if="row.progress==2">等待核实金额
                            </el-button>
                            <el-button type="text" @click="copy('等待缴费')"
                                       v-else-if="row.progress==3">等待缴费
                            </el-button>
                            <el-button type="text" @click="copy('缴费完成')"
                                       v-else-if="row.progress==4">缴费完成
                            </el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="building" label="楼栋" min-width="120" sortable align="center"/>
                <el-table-column prop="unit" label="单元" min-width="120" sortable align="center"/>
                <el-table-column prop="room" label="房间" min-width="120" sortable align="center"/>
                <el-table-column prop="creationTime" label="创建时间" min-width="220" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy(new Date(row.creationTime).toLocaleString())">
                                {{ new Date(row.creationTime).toLocaleString() }}
                            </el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="缴费金额" min-width="140" align="center"/>
                <!-- 操作列 -->
                <el-table-column prop="paymentOrder" label="操作" v-if="isToConfirm"
                                 fixed="right" width="200" align="center">
                    <template #default="{row}">
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="paymentInfo(row)">详情
                        </el-button>
                        <el-button type="success" size="small" :icon="SuccessFilled"
                                   @click="paymentConfirm(row)" v-if="isToConfirm">确认
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="paymentOrder" label="操作" v-else
                                 fixed="right" width="120" align="center">
                    <template #default="{row}">
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="paymentInfo(row)">详情
                        </el-button>
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
    <!-- 缴费详情抽屉 -->
    <el-drawer v-model="infoDrawerVisible"
               title="缴费详情"
               :direction="'rtl'"
               :with-header="true"
               size="30%"
    >
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">缴费单号[ {{ paymentInfoDrawerData.paymentId }} ]的详情信息</h4>
        </template>
        <!-- 使用描述列表el-descriptions显示内容，字段全部可以可以一键复制 -->
        <el-descriptions :column="1" border>
            <el-descriptions-item label="记录序号">{{ paymentInfoDrawerData.paymentOrder }}</el-descriptions-item>
            <el-descriptions-item label="缴费单号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(paymentInfoDrawerData.paymentId)">
                            {{ paymentInfoDrawerData.paymentId }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="房间号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(paymentInfoDrawerData.room)">{{
                                paymentInfoDrawerData.room
                            }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(paymentInfoDrawerData.creationTime)">
                            {{ paymentInfoDrawerData.creationTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="缴费金额">{{ paymentInfoDrawerData.amount }}</el-descriptions-item>
            <el-descriptions-item label="缴费描述">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(paymentInfoDrawerData.description)">
                            {{ paymentInfoDrawerData.description }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="缴费时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(paymentInfoDrawerData.paymentTime)">
                            {{ paymentInfoDrawerData.paymentTime }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="付款人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(paymentInfoDrawerData.payer)">
                            {{ paymentInfoDrawerData.payer }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="付款方式">{{ paymentInfoDrawerData.paymentMethod }}</el-descriptions-item>
            <el-descriptions-item label="缴费进度">
                <template #default>
                    <el-steps :active="paymentInfoDrawerData.progress<4?paymentInfoDrawerData.progress-1:4"
                              finish-status="success"
                              align-center
                              direction="vertical"
                    >
                        <el-step title="订单创建" :icon="List"></el-step>
                        <el-step title="等待核实金额" :icon="Tools"></el-step>
                        <el-step title="等待缴费" :icon="Money"></el-step>
                        <el-step title="缴费完成" :icon="SuccessFilled"></el-step>
                    </el-steps>
                </template>
            </el-descriptions-item>
        </el-descriptions>
        <!-- 底部确认按钮 -->
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="infoDrawerVisible = false">完成</el-button>
            </span>
        </template>
    </el-drawer>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>