<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserName, getUserGroup} from "@/views/scripts/UserUtils";
import {post, managerDeletePayment, getPaymentInfo} from "@/net";
import {ElMessage, ElTable} from "element-plus";
import {
    Avatar,
    Checked, CircleCloseFilled, CirclePlus,
    Delete,
    Edit, InfoFilled, List, Money, Paperclip, QuestionFilled,
    Refresh,
    Search,
    SuccessFilled,
    Tools, WarnTriangleFilled
} from "@element-plus/icons-vue";
import {copy} from "@/views/scripts/ManageUtils";
import {ElMessageBox} from 'element-plus'

const userName = getUserName();
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const small = ref(false);
const total = ref(0);
const background = ref(true);
const pageSizes = ref([1, 2, 10, 20, 30, 50, 100]);
let searchFormVisible = ref(false)
let infoDrawerVisible = ref(false)
let dialogFormVisible = ref(false)
let dialogAddNewFormVisible = ref(false)
const formLabelWidth = '140px'
const formLabelItemWidth = '120px'
const loading = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const addNewFormRef = ref()
const searchRef = ref()
const formRef = ref()

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
    document.title = "缴费管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const shortcuts = [
    {
        text: '最近1周',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
        },
    },
    {
        text: '最近30天',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
        },
    },
    {
        text: '最近90天',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
        },
    },
]

// 高级搜索表单
const searchForm = reactive({
    paymentOrder: '',
    paymentId: '',
    building: '',
    unit: '',
    room: '',
    // 如果是true，那么就是按照创建时间搜索，否则按照缴费时间搜索
    isCreationTime: true,
    // 通过日期区间搜索
    timeRange: [],
    payer: '',
    paymentMethod: '',
})

const searchRule = {
    paymentOrder: [
        {type: 'number', message: '缴费单号必须为数字', trigger: 'blur'}
    ]
}

const addPaymentForm =  reactive({
    building: '',
    unit: '',
    room: '',
    amount: 0,
    description: ''
})

const addPaymentRule = {
    building: [
        {required: true, message: '请选择楼栋号', trigger: 'blur'}
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'blur'}
    ],
    room: [
        {required: true, message: '请输入宿舍号', trigger: 'blur'}
    ],
    amount: [
        {required: true, message: '缴费金额不能为空', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '缴费描述不能为空', trigger: 'blur'}
    ]
}

const changeInfoForm = reactive({
    paymentOrder: 0,
    paymentId: '',
    building: '',
    unit: '',
    room: '',
    amount: 0,
    description: '',
    progress: 0
})

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

const updateRule = {}

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

const selectionRows = (rows?: Payment[]) => {
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

// 将getPaymentInfo封装为一个函数，方便调用
function updateInfoTable() {
    searchFormVisible.value = false;
    loading.value = true;
    const url = '/api/payment/list/all?page=' + currentPage.value + '&pageSize=' + pageSize.value;
    post(url, {...searchForm}, (data) => {
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

// 编辑缴费单浮窗
function editPayment(row: Payment) {
    console.log("editPayment:" + row.creationTime.valueOf());
    changeInfoForm.paymentOrder = row.paymentOrder;
    changeInfoForm.paymentId = row.paymentId;
    changeInfoForm.building = row.building;
    changeInfoForm.unit = row.unit;
    changeInfoForm.room = row.room;
    changeInfoForm.amount = row.amount;
    changeInfoForm.description = row.description;
    changeInfoForm.progress = row.progress;
    dialogFormVisible.value = true;
}

function deletePayment(row: Payment) {
    console.log("deletePayment:" + row.paymentOrder);
    createDialog("删除确认", "确认删除缴费单号为" + row.paymentId + "的缴费单吗？").then((value) => {
        if (value) {
            managerDeletePayment(row.paymentOrder, () => {
                ElMessage.success("删除成功！")
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("删除失败，", error);
            })
        }
    })
}

// 添加缴费单
function addNewPayment() {
    addNewFormRef.value.validate((valid) => {
        if (valid) {
            post('/api/payment/create', {...addPaymentForm}, (data) => {
                const paymentId = data.paymentId
                ElMessage.success("缴费单"+paymentId+"创建成功！")
                dialogAddNewFormVisible.value = false;
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("添加失败，", error);
            })
        } else {
            ElMessage.warning("请填写完整表单内容")
        }
    })
}

async function isFormValid() {
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 查看缴费单详情
function paymentInfo(row: Payment) {
    getPaymentInfo(row.paymentId, (data) => {
        paymentInfoDrawerData.paymentOrder = data.paymentOrder;
        paymentInfoDrawerData.paymentId = data.paymentId;
        paymentInfoDrawerData.room = data.building + data.unit + data.room;
        // 把时间戳转换为当前时区的时间字符串，如果为空则不转换
        if (data.creationTime != null) {
            paymentInfoDrawerData.creationTime = new Date(data.creationTime).toLocaleString();
        }
        if (data.paymentTime != null) {
            paymentInfoDrawerData.paymentTime = new Date(data.paymentTime).toLocaleString();
        }
        paymentInfoDrawerData.amount = data.amount;
        paymentInfoDrawerData.description = data.description;
        paymentInfoDrawerData.payer = data.payer;
        paymentInfoDrawerData.paymentMethod = data.paymentMethod;
        paymentInfoDrawerData.progress = data.progress;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取缴费详情失败", error);
    });
}

// 等待对话框关闭后，更新表格
function updatePayment() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/payment/edit', {...changeInfoForm}, () => {
                ElMessage.success("修改成功！")
                dialogFormVisible.value = false;
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("修改失败，", error);
            })
        } else {
            ElMessage.warning("请填写完整表单内容")
        }
    })
}

</script>

<template>
    <div class="table-container">
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="primary" size="small" :icon="Search" @click="searchFormVisible = true">高级搜索</el-button>
                <el-button type="primary" size="small" :icon="CirclePlus" @click="dialogAddNewFormVisible = true">添加账单</el-button>
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
            </div>
            <!-- 高级搜索浮窗 -->
            <el-dialog v-model="searchFormVisible" title="高级搜索">
                <el-form model="searchForm" :rules="searchRule" ref="searchRef">
                    <el-form-item label="记录序号" :label-width="formLabelWidth" prop="paymentOrder">
                        <el-input v-model="searchForm.paymentOrder" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费
单号" :label-width="formLabelWidth" prop="paymentId">
                        <el-input v-model="searchForm.paymentId" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费
楼栋" :label-width="formLabelWidth" prop="building">
                        <el-input v-model="searchForm.building" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费
单元" :label-width="formLabelWidth" prop="unit">
                        <el-input v-model="searchForm.unit" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费
房间" :label-width="formLabelWidth" prop="room">
                        <el-input v-model="searchForm.room" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="检索时间" :label-width="formLabelWidth" prop="isCreationTime">
                        <el-radio-group v-model="searchForm.isCreationTime">
                            <el-radio :label="true">创建时间</el-radio>
                            <el-radio :label="false">缴费时间</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="查询间隔" :label-width="formLabelWidth" prop="timeRange">
                        <el-date-picker
                            v-model="searchForm.timeRange"
                            type="datetimerange"
                            style="width: 120px"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            :shortcuts="shortcuts"
                            range-separator="到"
                            date-format="YYYY/MM/DD"
                            time-format="hh:mm:ss"
                        />
                    </el-form-item>
                    <el-form-item label="付款人" :label-width="formLabelWidth" prop="payer">
                        <el-input v-model="searchForm.payer" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="付款方式" :label-width="formLabelWidth" prop="paymentMethod">
                        <el-input v-model="searchForm.paymentMethod" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="searchFormVisible = false">取消</el-button>
                        <el-button type="primary" @click="updateInfoTable">搜索</el-button>
                    </span>
                </template>
            </el-dialog>
            <!-- 新增条目浮窗，里面有一个表单，表单中有需要填写的信息、确认按钮和取消按钮 -->
            <el-dialog v-model="dialogAddNewFormVisible" title="新增缴费单">
                <el-form :model="addPaymentForm" :rules="addPaymentRule" ref="addNewFormRef" status-icon>
                    <el-form-item label="楼栋" :label-width="formLabelWidth" prop="building">
                        <el-select v-model="addPaymentForm.building" :label-width="formLabelItemWidth"
                                   placeholder="请选择楼栋号">
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
                    <el-form-item label="单元" :label-width="formLabelWidth" prop="unit">
                        <el-select v-model="addPaymentForm.unit" :label-width="formLabelItemWidth"
                                   placeholder="请选择单元号">
                            <el-option label="A单元" value="A"/>
                            <el-option label="B单元" value="B"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="房间" :label-width="formLabelWidth" prop="room">
                        <el-input v-model="addPaymentForm.room" style="width: 240px"
                                  maxlength="3" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费金额" :label-width="formLabelWidth" prop="amount">
                        <el-input-number v-model="addPaymentForm.amount" :precision="2" :min="0"
                                         style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="缴费描述" :label-width="formLabelWidth" prop="description">
                        <el-input type="textarea" v-model="addPaymentForm.description"
                                  :autosize="{ minRows: 2, maxRows: 4}"
                                  style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
                        <el-button type="primary" @click="addNewPayment">确认</el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
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
            <el-table-column prop="paymentId" label="缴费
单号" min-width="200" sortable align="center">
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
                                   v-if="row.progress==2">等待核实金额</el-button>
                        <el-button type="text" @click="copy('等待缴费')"
                                   v-else-if="row.progress==3">等待缴费</el-button>
                        <el-button type="text" @click="copy('缴费完成')"
                                   v-else-if="row.progress==4">缴费完成</el-button>
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
            <el-table-column prop="paymentOrder" label="操作" fixed="right" width="300" align="center">
                <template #default="{row}">
                    <el-button type="info" size="small" :icon="InfoFilled" @click="paymentInfo(row)">详情</el-button>
                    <el-button type="primary" size="small" :icon="Edit" @click="editPayment(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete"
                               @click="deletePayment(row)" v-if="row.progress<4">删除</el-button>
                        <el-button type="danger" size="small" :icon="Delete" title="已缴费完成，无法删除"
                                   @click="deletePayment(row)" v-else disabled>删除</el-button>
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
    <!-- 修改浮窗，里面有一个表单、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="缴费单信息修改">
        <el-form :model="changeInfoForm" :rules="updateRule" ref="formRef">
            <el-form-item label="记录序号"
                          :label-width="formLabelWidth"
                          prop="paymentOrder">
                <el-input v-model="changeInfoForm.paymentOrder"
                          style="width: 240px"
                          autocomplete="off"
                          disabled/>
            </el-form-item>
            <el-form-item label="缴费单号"
                          :label-width="formLabelWidth"
                          prop="paymentId">
                <el-input v-model="changeInfoForm.paymentId"
                          style="width: 240px"
                          autocomplete="off"
                          disabled/>
            </el-form-item>
            <el-form-item label="楼栋"
                          :label-width="formLabelWidth" prop="building">
                <el-select v-model="changeInfoForm.building"
                           :label-width="formLabelItemWidth"
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
            <el-form-item label="单元"
                          :label-width="formLabelWidth" prop="unit">
                <el-select v-model="changeInfoForm.unit"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择单元号"
                >
                    <el-option label="A单元" value="A"/>
                    <el-option label="B单元" value="B"/>
                </el-select>
            </el-form-item>
            <el-form-item label="房间" :label-width="formLabelWidth" prop="room">
                <el-input v-model="changeInfoForm.room" :label-width="formLabelItemWidth" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="缴费金额"
                          :label-width="formLabelWidth"
                          prop="amount">
                <el-input v-model="changeInfoForm.amount"
                          style="width: 240px"
                          autocomplete="off"/>
            </el-form-item>
            <el-form-item label="缴费描述"
                          :label-width="formLabelWidth"
                          prop="description">
                <el-input v-model="changeInfoForm.description"
                          style="width: 240px"
                          autocomplete="off"/>
            </el-form-item>
            <el-form-item label="缴费进度"
                          :label-width="formLabelWidth"
                          prop="progress">
                <!-- 下拉选择框 -->
                <el-select v-model="changeInfoForm.progress"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择当前进度"
                >
                    <el-option label="订单创建" value="1"></el-option>
                    <el-option label="等待核实金额" value="2"></el-option>
                    <el-option label="等待缴费" value="3"></el-option>
                    <el-option label="缴费完成" value="4"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
            <el-button @click="isFormValid">检查</el-button>
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="updatePayment" :disabled="!isFormValid">确认</el-button>
        </span>
        </template>
    </el-dialog>
</template>


<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>