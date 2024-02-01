<script lang="ts" setup>
import QRCode from 'qrcode';
import QrcodeVue from 'qrcode.vue';
import {reactive, ref, watch} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {get, post, getStaffDetail, getPaymentInfo, confirmPayment} from "@/net";
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
const paidFormRef = ref();
let userId = getUserId();
let infoDrawerVisible = ref(false);
let paidDialogVisable = ref(false);
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const pageSizes = ref([1, 2, 5, 10, 20, 30, 50, 100]);
const background = ref(true);
const small = ref(false);
let qrCode: QRCode = '';

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
    document.title = "水电缴费" + " - HMS(" + userGroup.value + ")";
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

// 将 getPaymentConfirm 封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    const url = '/api/payment/list/room/' + userId + '/paid?'
        + 'page=' + currentPage.value
        + '&pageSize=' + pageSize.value;
    get(url, (data) => {
        console.log("获取数据成功", data);
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

updateInfoTable();

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

const paidDialogFormData = reactive({
    paymentId: '',
    amount: 0,
    description: '',
    paymentMethod: '',
    payer: userId
})

const paidDialogFormRules = {}

// 查看缴费单详情
function paymentInfo(row: Payment) {
    // 从接口getPaymentInfoByOrder得到的json的data中获取选择的用户组权限信息
    getPaymentInfo(row.paymentId, (data) => {
        console.log("data:", data);
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

function openPaidPage(row: Payment) {
    paidDialogFormData.paymentId = row.paymentId;
    paidDialogFormData.amount = row.amount;
    paidDialogFormData.description = row.description;
    paidDialogFormData.paymentMethod = row.paymentMethod;
    paidDialogFormData.payer = row.payer;
    paidDialogVisable.value = true;
}

function finishPaid(){
    if (paidDialogFormData.paymentMethod == '') {
        ElMessage.warning("请选择付款方式")
        return;
    }
    paidDialogFormData.payer = getUserId();
    console.log("paidDialogFormData:", paidDialogFormData);
    post('/api/payment/paid', {...paidDialogFormData}, (data) => {
        console.log("data:", data);
        ElMessage.success("缴费成功");
        paidDialogVisable.value = false;
        updateInfoTable();
    }, (error) => {
        console.error("缴费失败", error);
        ElMessage.error("缴费失败");
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
        <div class="table-container">
            <el-table :data="tableData"
                      class-name="table-main"
                      empty-text="未查询到数据！"
                      header-align="center"
                      ref="multipleTableRef"
                      v-loading="loading"
                      v-if="mainTableVisable"
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
                <el-table-column prop="paymentOrder" label="操作" fixed="right" width="200" align="center">
                    <template #default="{row}">
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="paymentInfo(row)">详情
                        </el-button>
                        <el-button type="success" size="small" :icon="Money"
                                   @click="openPaidPage(row)" v-if="row.progress == 3">去付款
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
                        <el-step title="等待核实金额" :icon="QuestionFilled"></el-step>
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
    <!-- 支付窗口，包括显示金额，付款描述，一个二维码（图片显示框），两个按钮（微信支付、支付宝支付）和确认支付按钮 -->
    <el-dialog v-model="paidDialogVisable" title="账单支付" style="width: 460px;height: 520px">
        <el-form :model="paidDialogFormData" :rules="paidDialogFormRules" ref="paidFormRef"
                 label-width="80px">
            <!-- 头部 -->
            <div style="height: 40px; line-height: 40px; text-align: center">
                <h2 style="color: #f56c6c;font-size: 33px">￥{{ paidDialogFormData.amount }}</h2>
            </div>
            <!-- 付款描述及二维码、单号 -->
            <div style="height: 200px; text-align: center;margin-top: 20px;">
                <!-- 选择付款方式，绑定到表单的paymentMethod -->
                <el-radio-group v-model="paidDialogFormData.paymentMethod">
                    <el-radio border label="微信" style="height: 40px">
                        <img src="./../../img/wechat_icon.svg"
                             style="width:24px; height:24px;position: relative;left: -2px;top: 2px;"/>
                        <span style="position: relative;top: -4px;left: 3px">微信</span>
                    </el-radio>
                    <el-radio border label="支付宝" style="height: 40px">
                        <img src="./../../img/alipay_icon.svg"
                             style="width:24px; height:24px;position: relative;left: -2px;top: 2px;"/>
                        <span style="position: relative;top: -4px;left: 3px">支付宝</span>
                    </el-radio>
                </el-radio-group>
                <!-- 账单描述，如果太长则显示省略号，只显示一行 -->
                <p style="color: #909399;font-size: 14px; overflow: hidden;
                text-overflow: ellipsis; white-space: nowrap">
                    账单描述：{{ paidDialogFormData.description }}
                </p>
                <!-- 二维码，使用qrcode库生成二维码，未选择付款方式时则为提示框 -->
                <div style="height: 180px; width: 180px; margin: 0 auto" @click="finishPaid()">
                    <!--                    <img v-if="paidDialogFormData.paymentMethod == '微信' || paidDialogFormData.paymentMethod == '支付宝'"-->
                    <!--                         :src="qrCode" style="width: 180px; height: 180px"/>-->
                    <!--                    <QRCodeVue3 value="https://www.bing.com"
                                                    :height="180"
                                                    :width="180"

                                                    v-if="paidDialogFormData.paymentMethod == '微信' || paidDialogFormData.paymentMethod == '支付宝'"
                                        />-->
                    <qrcode-vue v-if="paidDialogFormData.paymentMethod == '微信'"
                                :value="'Amount: '+paidDialogFormData.amount+'Yuan, ' +
                                  'Bill description: '+paidDialogFormData.description + ', ' +
                                  'Bill Id: ' + paidDialogFormData.paymentId + ', ' +
                                  'Payment Method: Wechat'"
                                size:300
                                style="width: 180px; height: 180px"
                    />
                    <qrcode-vue v-else-if="paidDialogFormData.paymentMethod == '支付宝'"
                                :value="'金额：'+paidDialogFormData.amount+'元，' +
                                  '账单描述：'+paidDialogFormData.description + '，' +
                                  '缴费单号：' + paidDialogFormData.paymentId + '，' +
                                  '缴费方式：' + paidDialogFormData.paymentMethod"
                                size:300
                                style="width: 180px; height: 180px"
                    />
                    <p v-else style=" color: #909399;font-size: 14px; line-height: 120px
                    ">请先选择付款方式</p>
                </div>
                <!-- 缴费单号，灰色小字体 -->
                <p style="color: #909399;font-size: 12px">缴费单号：{{ paidDialogFormData.paymentId }}</p>
            </div>
        </el-form>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>