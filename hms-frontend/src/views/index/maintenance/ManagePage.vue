<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserName, getUserGroup} from "@/views/scripts/UserUtils";
import {post, managerCloseRepair, getRepairInfoByOrder, toFinishRepair} from "@/net";
import {ElMessage, ElTable} from "element-plus";
import {
    Avatar,
    Checked, CircleCloseFilled,
    Delete,
    Edit, InfoFilled, List, Paperclip, QuestionFilled,
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
const formLabelWidth = '140px'
const formLabelItemWidth = '120px'
const loading = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
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
    document.title = "报修管理" + " - HMS(" + userGroup.value + ")";
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
const form = reactive({
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
    // 如果是true，那么就是按照创建时间搜索，否则按照完成时间搜索
    isCreationTime: true,
    // 通过日期区间搜索
    timeRange: [],
    worker: '',
    dormManager: '',
    progress: null
})

const searchRule = {
    repairOrder: [
        {type: 'number', message: '报修单号必须为数字值', trigger: 'blur'}
    ],
    progress: [
        {type: 'number', message: '进度必须为数字值', trigger: 'blur'}
    ]
}

const changeInfoForm = reactive({
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
    creationTime: 0,
    finishTime: 0,
    worker: '',
    dormManager: '',
    progress: 0
})

const repairInfoDrawerData = reactive({
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

const updateRule = {
    email: [
        {type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
    ],
    phone: [
        {min: 11, max: 11, message: '手机号必须为11位数字', trigger: 'blur'}
    ]
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

const selectionRows = (rows?: Repair[]) => {
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
    creationTime: number,
    finishTime: number,
    worker: string,
    dormManager: string,
    progress: number
}

// 将getRepairInfo封装为一个函数，方便调用
function updateInfoTable() {
    searchFormVisible.value = false;
    loading.value = true;
    const url = '/api/repair/list/all?page=' + currentPage.value + '&pageSize=' + pageSize.value;
    post(url, {...form}, (data) => {
        // 等待随机时间后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            // 将每一组数据的创建时间和完成时间转换为当前时区的时间字符串，如果为空则不转换
            // data.data.forEach((item: Repair) => {
            //     if (item.creationTime != null) {
            //         item.creationTime = new Date(item.creationTime).toLocaleString();
            //     }
            //     if (item.finishTime != null) {
            //         item.finishTime = new Date(item.finishTime).toLocaleString();
            //     }
            // })
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

// 关闭报修记录
function closeRepair(row: Repair) {
    console.log("closeRepair:" + row.repairOrder);
    createDialog("关闭报修单", "确认强制关闭报修单" + row.repairOrder + "吗？").then((res) => {
        if (res) {
            managerCloseRepair(row.repairOrder, 7, (data) => {
                console.log("data:", data);
                ElMessage.success("报修记录" + row.repairOrder + "关闭成功");
                updateInfoTable();
            }, (error) => {
                console.error("关闭报修单失败", error);
                ElMessage.error("报修记录" + row.repairOrder + "关闭失败");
            });
        }
    }, reason => {
        console.log("取消关闭报修单");
    })
}

// 编辑报修记录浮窗
function editRepair(row: Repair) {
    console.log("editRepair:" + row.creationTime.valueOf());
    changeInfoForm.repairOrder = row.repairOrder;
    changeInfoForm.reporter = row.reporter;
    changeInfoForm.building = row.building;
    changeInfoForm.unit = row.unit;
    changeInfoForm.room = row.room;
    changeInfoForm.type = row.type;
    changeInfoForm.description = row.description;
    changeInfoForm.requirement = row.requirement;
    changeInfoForm.email = row.email;
    changeInfoForm.phone = row.phone;
    changeInfoForm.creationTime = row.creationTime;
    changeInfoForm.finishTime = row.finishTime;
    changeInfoForm.worker = row.worker;
    changeInfoForm.dormManager = row.dormManager;
    changeInfoForm.progress = row.progress;
    dialogFormVisible.value = true;
}

async function isFormValid() {
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 查看报修详情
function repairInfo(row: Repair) {
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
}

// 等待对话框关闭后，更新表格
function updateRepair() {

    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/repair/update', {...changeInfoForm}, () => {
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
    updateInfoTable();
}

function finishRepair(row: Repair){
    console.log("finishRepair:" + row.repairOrder);
    toFinishRepair(row.repairOrder, userName, (data) => {
        console.log("data:", data);
        ElMessage.success("报修单" + row.repairOrder + "已维修完成");
        updateInfoTable();
    }, (error) => {
        console.error("完成报修单失败", error);
        ElMessage.error("报修单" + row.repairOrder + "记录失败");
    });
}

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

</script>

<template>
    <div class="table-container">
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="primary" size="small" :icon="Search" @click="searchFormVisible = true">高级搜索
                </el-button>
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
            </div>
            <!-- 高级搜索浮窗 -->
            <el-dialog v-model="searchFormVisible" title="高级搜索">
                <el-form v-model="form" :rules="searchRule" ref="searchRef">
                    <el-form-item label="报修单号" :label-width="formLabelWidth" prop="repairOrder">
                        <el-input v-model="form.repairOrder" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修人" :label-width="formLabelWidth" prop="reporter">
                        <el-input v-model="form.reporter" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修地点" :label-width="formLabelWidth" prop="building">
                        <el-input v-model="form.building" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="单元" :label-width="formLabelWidth" prop="unit">
                        <el-input v-model="form.unit" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="房间号" :label-width="formLabelWidth" prop="room">
                        <el-input v-model="form.room" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修类型" :label-width="formLabelWidth" prop="type">
                        <el-input v-model="form.type" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修描述" :label-width="formLabelWidth" prop="description">
                        <el-input v-model="form.description" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修要求" :label-width="formLabelWidth" prop="requirement">
                        <el-input v-model="form.requirement" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修人邮箱" :label-width="formLabelWidth" prop="email">
                        <el-input v-model="form.email" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="报修人手机号" :label-width="formLabelWidth" prop="phone">
                        <el-input v-model="form.phone" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="检索时间" :label-width="formLabelWidth" prop="isCreationTime">
                        <el-radio-group v-model="form.isCreationTime">
                            <el-radio :label="true">创建时间</el-radio>
                            <el-radio :label="false">完成时间</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="查询间隔" :label-width="formLabelWidth" prop="timeRange">
                        <el-date-picker
                            v-model="form.timeRange"
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
                    <el-form-item label="维修负责人" :label-width="formLabelWidth" prop="worker">
                        <el-input v-model="form.worker" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="楼栋负责人" :label-width="formLabelWidth" prop="dormManager">
                        <el-input v-model="form.dormManager" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="维修状态" :label-width="formLabelWidth" prop="progress">
                        <el-input v-model="form.progress" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="searchFormVisible = false">取消</el-button>
                        <el-button type="primary" @click="updateInfoTable">搜索</el-button>
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
            <el-table-column prop="creationTime" label="报修时间" min-width="220" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(new Date(row.creationTime).toLocaleString())">
                            {{ new Date(row.creationTime).toLocaleString() }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="progress" label="报修进度" min-width="220" sortable align="center">
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
            <el-table-column prop="repairOrder" label="报修单号" min-width="140" sortable align="center"/>
            <el-table-column prop="building" label="报修楼栋" min-width="140" sortable align="center"/>
            <el-table-column prop="unit" label="报修单元" min-width="140" sortable align="center"/>
            <el-table-column prop="room" label="报修房间" min-width="140" sortable align="center"/>
            <el-table-column prop="type" label="报修类型" min-width="140" sortable align="center"/>
            <el-table-column prop="requirement" label="报修要求" min-width="140" sortable align="center"/>
            <!-- 操作列 -->
            <el-table-column prop="repairOrder" label="操作" fixed="right" width="360" align="center">
                <template #default="{row}">
                    <el-button type="default" size="small" :icon="Paperclip"
                               @click="finishRepair(row)" v-if="row.progress==3">完成</el-button>
                    <el-button type="default" size="small" :icon="Paperclip"
                               @click="" v-else disabled>完成</el-button>
                    <el-button type="info" size="small" :icon="InfoFilled" @click="repairInfo(row)">详情</el-button>
                    <el-button type="primary" size="small" :icon="Edit" @click="editRepair(row)">编辑</el-button>
                    <el-button type="warning" size="small" :icon="WarnTriangleFilled"
                               @click="closeRepair(row)" v-if="row.progress<5">结束</el-button>
                    <el-button type="warning" size="small" :icon="WarnTriangleFilled"
                                v-else disabled>结束</el-button>
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
    <!-- 修改浮窗，里面有一个表单、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="报修记录信息修改">
        <el-form :model="changeInfoForm" :rules="updateRule" ref="formRef">
            <el-form-item label="报修单号" :label-width="formLabelWidth" prop="repairOrder">
                <el-input v-model="changeInfoForm.repairOrder" :label-width="formLabelItemWidth" autocomplete="off"
                          disabled/>
            </el-form-item>
            <el-form-item label="报修人id" :label-width="formLabelWidth" prop="reporter">
                <el-input v-model="changeInfoForm.reporter" :label-width="formLabelItemWidth" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="报修楼栋"
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
            <el-form-item label="报修单元"
                          :label-width="formLabelWidth" prop="unit">
                <el-select v-model="changeInfoForm.unit"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择单元号"
                >
                    <el-option label="A单元" value="A"/>
                    <el-option label="B单元" value="B"/>
                </el-select>
            </el-form-item>
            <el-form-item label="报修房间" :label-width="formLabelWidth" prop="room">
                <el-input v-model="changeInfoForm.room" :label-width="formLabelItemWidth" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="报修类型"
                          :label-width="formLabelWidth" prop="type">
                <el-select v-model="changeInfoForm.type"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择报修类型"
                >
                    <el-option label="网络" value="网络"/>
                    <el-option label="电器" value="电器"/>
                    <el-option label="家具" value="家具"/>
                    <el-option label="其他" value="其他"/>
                </el-select>
            </el-form-item>
            <el-form-item label="报修描述"
                          :label-width="formLabelWidth"
                          prop="description">
                <el-input v-model="changeInfoForm.description"
                          type="textarea"
                          placeholder="请输入报修描述"
                          :label-width="formLabelItemWidth"
                />
            </el-form-item>
            <el-form-item label="报修需求"
                          :label-width="formLabelWidth"
                          prop="requirement">
                <!-- 维修、更换等 -->
                <el-radio-group v-model="changeInfoForm.requirement"
                                :label-width="formLabelItemWidth">
                    <el-radio-button label="维修">维修</el-radio-button>
                    <el-radio-button label="更换">更换</el-radio-button>
                    <el-radio-button label="检查">检查</el-radio-button>
                    <el-radio-button label="其他">其他</el-radio-button>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="邮箱"
                          :label-width="formLabelWidth"
                          prop="email">
                <el-input v-model="changeInfoForm.email"
                          placeholder="请输入报修人邮箱"
                          :label-width="formLabelItemWidth"
                />
            </el-form-item>
            <el-form-item label="手机号"
                          :label-width="formLabelWidth"
                          prop="phone">
                <el-input v-model="changeInfoForm.phone"
                          placeholder="请输入报修人手机号"
                          :label-width="formLabelItemWidth"
                />
            </el-form-item>
            <el-form-item label="报修时间"
                          :label-width="formLabelWidth"
                          prop="creationTime">
                <el-date-picker v-model="changeInfoForm.creationTime"
                                placeholder="请输入报修时间"
                                type="datetime"
                                :label-width="formLabelItemWidth"
                                format="YYYY-MM-DD HH:mm:ss"
                                disabled
                />
            </el-form-item>
            <el-form-item label="完成时间"
                          :label-width="formLabelWidth"
                          prop="finishTime">
                <el-date-picker v-model="changeInfoForm.finishTime"
                                placeholder="完成时间"
                                type="datetime"
                                :label-width="formLabelItemWidth"
                                format="YYYY-MM-DD HH:mm:ss"
                                disabled
                />
            </el-form-item>
            <el-form-item label="维修负责人"
                          :label-width="formLabelWidth"
                          prop="worker">
                <el-input v-model="changeInfoForm.worker"
                          placeholder="请输入维修负责人"
                          :label-width="formLabelItemWidth"
                />
            </el-form-item>
            <el-form-item label="楼栋负责人"
                          :label-width="formLabelWidth"
                          prop="dormManager">
                <el-input v-model="changeInfoForm.dormManager"
                          placeholder="请输入楼栋负责人"
                          :label-width="formLabelItemWidth"
                />
            </el-form-item>
            <el-form-item label="维修进度"
                          :label-width="formLabelWidth"
                          prop="progress">
                <!-- 下拉选择框 -->
                <el-select v-model="changeInfoForm.progress"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择当前进度"
                >
                    <el-option label="住户提交报修" value="1"/>
                    <el-option label="楼栋管理员进行确认" value="2"/>
                    <el-option label="正在维修" value="3"/>
                    <el-option label="维修完成，等待确认" value="4"/>
                    <el-option label="已完成" value="5"/>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateRepair" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
</template>


<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>