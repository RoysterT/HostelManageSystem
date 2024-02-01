<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils"
import {post,  getIdentityInfo, managerDeleteIdentity, getIdIdentity} from "@/net";
import {ElMessage, ElTable, inputNumberEmits} from "element-plus";
import {
    CircleCloseFilled,
    CirclePlus,
    Delete,
    Download,
    Edit,
    Loading,
    Refresh,
    Upload
} from "@element-plus/icons-vue";
import {copy} from "@/views/scripts/ManageUtils";
import {ElMessageBox} from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const small = ref(false);
const total = ref(0);
const background = ref(true);
const pageSizes = ref([1, 2, 3, 5, 10]);
let drawerFormVisible = ref(false)
const formLabelWidth = '100px'
const formLabelItemWidth = '200px'
const loading = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const formRef = ref()
let dialogAddNewFormVisible = ref(false)
const addNewFormRef = ref()
const drawer = ref(false)
const permitText = ref('允许')
const refuseText = ref('禁止')

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
    document.title = "用户组管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});


const changeInfoForm = reactive({
    id: 0,
    name: '',
    userInfo: false,
    userEdit: false,
    userPassword: false,
    userEmail: false,
    userPhone: false,
    accommodationInfo: false,
    accommodationApply: false,
    accommodationAudit: false,
    accommodationConfirm: false,
    inspectRules: false,
    inspectScore: false,
    inspectMark: false,
    inspectActivity: false,
    maintenanceApply: false,
    maintenanceHistory: false,
    maintenanceBuilding: false,
    maintenanceManage: false,
    paymentBill: false,
    paymentHistory: false,
    paymentBuilding: false,
    paymentManage: false,
    waterBill: false,
    waterHistory: false,
    waterSending: false,
    waterManage: false,
    systemPermission: false,
    systemAssignment: false,
    manageAccount: false,
    manageStudent: false,
    manageStaff: false,
    manageAccommodation: false
})

const addIdentityForm = reactive({
    id: total.value+1,
    name: '',
    userInfo: true,
    userEdit: true,
    userPassword: true,
    userEmail: true,
    userPhone: true,
    accommodationInfo: false,
    accommodationApply: false,
    accommodationAudit: false,
    accommodationConfirm: false,
    inspectRules: false,
    inspectScore: false,
    inspectMark: false,
    inspectActivity: false,
    maintenanceApply: false,
    maintenanceHistory: false,
    maintenanceBuilding: false,
    maintenanceManage: false,
    paymentBill: false,
    paymentHistory: false,
    paymentBuilding: false,
    paymentManage: false,
    waterBill: false,
    waterHistory: false,
    waterSending: false,
    waterManage: false,
    systemPermission: false,
    systemAssignment: false,
    manageAccount: false,
    manageStudent: false,
    manageStaff: false,
    manageAccommodation: false
})

const rule = {
    id: [
        {required: true, message: '请输入用户组id', trigger: 'blur'}
    ],
    name: [
        {required: true, message: '请输入名称', trigger: 'blur'}
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

const toggleSelection = (rows?: Identity[]) => {
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

const selectionRows = (rows?: Identity[]) => {
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

function cancelClick() {
    drawer.value = false
}

interface Identity {
    id: number,
    name: String,
    userInfo: boolean,
    userEdit: boolean,
    userPassword: boolean,
    userEmail: boolean,
    userPhone: boolean,
    accommodationInfo: boolean,
    accommodationApply: boolean,
    accommodationAudit: boolean,
    accommodationConfirm: boolean,
    inspectRules: boolean,
    inspectScore: boolean,
    inspectMark: boolean,
    inspectActivity: boolean,
    maintenanceApply: boolean,
    maintenanceHistory: boolean,
    maintenanceBuilding: boolean,
    maintenanceManage: boolean,
    paymentBill: boolean,
    paymentHistory: boolean,
    paymentBuilding: boolean,
    paymentManage: boolean,
    waterBill: boolean,
    waterHistory: boolean,
    waterSending: boolean,
    waterManage: boolean,
    systemPermission: boolean,
    systemAssignment: boolean,
    manageAccount: boolean,
    manageStudent: boolean,
    manageStaff: boolean,
    manageAccommodation: boolean
}

// 将getIdentityInfo封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    getIdentityInfo(currentPage.value, pageSize.value, (data) => {
        console.log("data:", data.data);
        // 为了让用户看到我做了加载动画，等待随机0.5-1.5秒后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            tableData.value = data.data;
            total.value = data.total;
            loading.value = false;
            // ElMessage.success("您本次浪费了"+(random/1000)+"秒看加载动画！");
        }, random);
    }, (error) => {
        console.error("获取用户详情失败", error);
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

// 删除用户组
function deleteIdentity(row: Identity) {
    console.log("deleteIdentity:" + row.id);
    createDialog("删除数据", "确认删除用户组" + row.id + "吗？").then((res) => {
        if (res){
            managerDeleteIdentity(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("用户组" + row.id + "删除成功");
                updateInfoTable();
            }, (error) => {
                console.error("删除用户组失败", error);
                ElMessage.error("用户组" + row.id + "删除失败");
            });
        }
    }, reason => {
        console.log("取消删除用户组" + row.id);
    });
}

// 批量删除用户组
function deleteIdentities() {

}

// 编辑用户组，从右侧弹出抽屉表单
function editIdentity(row: Identity) {
    console.log("editIdentity:" + row.id);
    // 从接口getIdIdentity得到的json的data中获取选择的用户组权限信息
    getIdIdentity(row.id, (data) => {
        console.log("data:", data);
        changeInfoForm.id = data.id;
        changeInfoForm.name = data.name;
        changeInfoForm.userInfo = data.userInfo;
        changeInfoForm.userEdit = data.userEdit;
        changeInfoForm.userPassword = data.userPassword;
        changeInfoForm.userEmail = data.userEmail;
        changeInfoForm.userPhone = data.userPhone;
        changeInfoForm.accommodationInfo = data.accommodationInfo;
        changeInfoForm.accommodationApply = data.accommodationApply;
        changeInfoForm.accommodationAudit = data.accommodationAudit;
        changeInfoForm.accommodationConfirm = data.accommodationConfirm;
        changeInfoForm.inspectRules = data.inspectRules;
        changeInfoForm.inspectScore = data.inspectScore;
        changeInfoForm.inspectMark = data.inspectMark;
        changeInfoForm.inspectActivity = data.inspectActivity;
        changeInfoForm.maintenanceApply = data.maintenanceApply;
        changeInfoForm.maintenanceHistory = data.maintenanceHistory;
        changeInfoForm.maintenanceBuilding = data.maintenanceBuilding;
        changeInfoForm.maintenanceManage = data.maintenanceManage;
        changeInfoForm.paymentBill = data.paymentBill;
        changeInfoForm.paymentHistory = data.paymentHistory;
        changeInfoForm.paymentBuilding = data.paymentBuilding;
        changeInfoForm.paymentManage = data.paymentManage;
        changeInfoForm.waterBill = data.waterBill;
        changeInfoForm.waterHistory = data.waterHistory;
        changeInfoForm.waterSending = data.waterSending;
        changeInfoForm.waterManage = data.waterManage;
        changeInfoForm.systemPermission = data.systemPermission;
        changeInfoForm.systemAssignment = data.systemAssignment;
        changeInfoForm.manageAccount = data.manageAccount;
        changeInfoForm.manageStudent = data.manageStudent;
        changeInfoForm.manageStaff = data.manageStaff;
        changeInfoForm.manageAccommodation = data.manageAccommodation;
    }, (error) => {
        console.error("获取用户组信息失败", error);
    });
    drawerFormVisible.value = true;
}

// 新增用户组浮窗
function addNewIdentity() {
    dialogAddNewFormVisible.value = true;

}

// 等待对话框关闭后，更新表格
function updateIdentity() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/system/permission/edit', {...changeInfoForm}, (data) => {
                ElMessage.success("修改成功！")
                drawerFormVisible.value = false;
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

// 等待增加账户对话框关闭后，更新表格
function createNewIdentity() {
    addNewFormRef.value.validate((valid) => {
        if (valid) {
            post('/api/system/permission/add', {...addIdentityForm}, (data) => {
                ElMessage.success("新增成功！")
                dialogAddNewFormVisible.value = false;
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("新增失败，", error);
            })
        } else {
            ElMessage.warning("请填写完整表单内容")
        }
    })
    updateInfoTable();
}

</script>

<template>
    <div class="table-container">
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
                <el-button type="primary" size="small" :icon="CirclePlus" @click="addNewIdentity">添加用户组</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="" disabled>下载模板文件</el-button>
                <el-button type="primary" size="small" :icon="Upload" @click="" disabled>上传批量添加</el-button>
                <el-button type="danger" size="small" :icon="Delete" @click="deleteIdentities" disabled>批量删除
                </el-button>
                <el-button type="danger" size="small" :icon="Loading" @click="" disabled>批量重置密码</el-button>
            </div>
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
            <el-table-column prop="id" label="用户组代码" min-width="160" sortable align="center"/>
            // 显示为tag，点击后一键复制
            <el-table-column prop="name" label="用户组名称" min-width="160" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-tag type="info" @click="copy(row.name)">{{ row.name }}</el-tag>
                    </el-tooltip>
                </template>
            </el-table-column>
            <!-- 操作列，有两个按钮：编辑和删除，点击编辑按钮跳弹出编辑用户组浮窗，点击删除按钮弹出确认删除提示窗，然后删除用户组 -->
            <el-table-column prop="id" label="操作" fixed="right" width="200" align="center">
                <template #default="{row}">
                    <el-button type="warning" size="small" :icon="Edit" @click="editIdentity(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteIdentity(row)">删除</el-button>
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
                :locale="zhCn"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
    </div>
    <!-- 修改信息，从右侧弹出的抽屉 -->
    <el-drawer title="用户组信息修改"
               v-model="drawerFormVisible">
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">编辑用户组信息</h4>
        </template>
        <el-form :model="changeInfoForm" :rules="rule" ref="formRef">
            <!-- 身份组id和名称 -->
            <el-form-item label="用户组id" :label-width="formLabelWidth" prop="id">
                <el-input v-model="changeInfoForm.id" disabled/>
            </el-form-item>
            <el-form-item label="用户组名称" :label-width="formLabelWidth" prop="name">
                <el-input v-model="changeInfoForm.name"/>
            </el-form-item>
            <!-- 将权限分组显示，每组中通过开关设置权限的开关，每行只显示一个权限-->
            <!-- 账号设置组 -->
            <el-form-item label="账号设置" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="查看当前账户信息" :label-width="formLabelItemWidth" prop="user_info">
                        <el-switch v-model="changeInfoForm.userInfo"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="修改当前账户信息" :label-width="formLabelItemWidth" prop="user_edit">
                        <el-switch v-model="changeInfoForm.userEdit"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="修改当前账户密码" :label-width="formLabelItemWidth" prop="user_password">
                        <el-switch v-model="changeInfoForm.userPassword"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="修改当前账户邮箱" :label-width="formLabelItemWidth" prop="user_email">
                        <el-switch v-model="changeInfoForm.userEmail"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="修改当前账户手机号" :label-width="formLabelItemWidth" prop="user_phone">
                        <el-switch v-model="changeInfoForm.userPhone"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 住宿管理组 -->
            <el-form-item label="住宿管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="查看住宿信息" :label-width="formLabelItemWidth" prop="accommodation_info">
                        <el-switch v-model="changeInfoForm.accommodationInfo"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="提交换宿请求" :label-width="formLabelItemWidth" prop="accommodation_apply">
                        <el-switch v-model="changeInfoForm.accommodationApply"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="审核换宿请求（楼）" :label-width="formLabelItemWidth" prop="accommodation_audit">
                        <el-switch v-model="changeInfoForm.accommodationAudit"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="审核换宿请求（校）" :label-width="formLabelItemWidth" prop="accommodation_confirm">
                        <el-switch v-model="changeInfoForm.accommodationConfirm"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 卫生管理组 -->
            <el-form-item label="卫生管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="查看卫生规范条例" :label-width="formLabelItemWidth" prop="inspect_rules">
                        <el-switch v-model="changeInfoForm.inspectRules"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="宿舍卫生评级" :label-width="formLabelItemWidth" prop="inspect_score">
                        <el-switch v-model="changeInfoForm.inspectScore"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="卫生评分" :label-width="formLabelItemWidth" prop="inspect_mark">
                        <el-switch v-model="changeInfoForm.inspectMark"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="卫生活动管理" :label-width="formLabelItemWidth" prop="inspect_activity">
                        <el-switch v-model="changeInfoForm.inspectActivity"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"
                        />
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 报修管理组 -->
            <el-form-item label="报修管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="提交报修" :label-width="formLabelItemWidth" prop="maintenance_apply">
                        <el-switch v-model="changeInfoForm.maintenanceApply"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="查看报修历史" :label-width="formLabelItemWidth" prop="maintenance_history">
                        <el-switch v-model="changeInfoForm.maintenanceHistory"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="查看楼栋报修信息" :label-width="formLabelItemWidth"
                                  prop="maintenance_building">
                        <el-switch v-model="changeInfoForm.maintenanceBuilding"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="报修管理" :label-width="formLabelItemWidth" prop="maintenance_manage">
                        <el-switch v-model="changeInfoForm.maintenanceManage"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 缴费管理组 -->
            <el-form-item label="缴费管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="水电缴费" :label-width="formLabelItemWidth" prop="payment_bill">
                        <el-switch v-model="changeInfoForm.paymentBill"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="查看历史缴费" :label-width="formLabelItemWidth" prop="payment_history">
                        <el-switch v-model="changeInfoForm.paymentHistory"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="查看楼栋缴费信息" :label-width="formLabelItemWidth" prop="payment_building">
                        <el-switch v-model="changeInfoForm.paymentBuilding"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="缴费管理" :label-width="formLabelItemWidth" prop="payment_manage">
                        <el-switch v-model="changeInfoForm.paymentManage"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 饮用水管理组 -->
            <el-form-item label="饮用水管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="饮用水订购" :label-width="formLabelItemWidth" prop="water_bill">
                        <el-switch v-model="changeInfoForm.waterBill"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="查看订购记录" :label-width="formLabelItemWidth" prop="water_history">
                        <el-switch v-model="changeInfoForm.waterHistory"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="订购管理（送水）" :label-width="formLabelItemWidth" prop="water_sending">
                        <el-switch v-model="changeInfoForm.waterSending"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="订购管理（信息）" :label-width="formLabelItemWidth" prop="water_manage">
                        <el-switch v-model="changeInfoForm.waterManage"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 系统管理组 -->
            <el-form-item label="系统管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="用户组权限管理" :label-width="formLabelItemWidth" prop="system_permission">
                        <el-switch v-model="changeInfoForm.systemPermission"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="用户所属组管理" :label-width="formLabelItemWidth" prop="system_assignment">
                        <el-switch v-model="changeInfoForm.systemAssignment"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
            <!-- 数据管理组 -->
            <el-form-item label="数据管理" :label-width="formLabelWidth">
                <el-row>
                    <el-form-item label="管理账号数据" :label-width="formLabelItemWidth" prop="manage_account">
                        <el-switch v-model="changeInfoForm.manageAccount"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="管理学生数据" :label-width="formLabelItemWidth" prop="manage_student">
                        <el-switch v-model="changeInfoForm.manageStudent"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="管理职工数据" :label-width="formLabelItemWidth" prop="manage_staff">
                        <el-switch v-model="changeInfoForm.manageStaff"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
                <el-row>
                    <el-form-item label="管理住宿数据" :label-width="formLabelItemWidth" prop="manage_accommodation">
                        <el-switch v-model="changeInfoForm.manageAccommodation"
                                   inline-prompt
                                   :active-text="permitText"
                                   :inactive-text="refuseText"
                                   :active-value="true"
                                   :inactive-value="false"/>
                    </el-form-item>
                </el-row>
            </el-form-item>
        </el-form>
        <!-- 底部，取消和确认两个按钮 -->
        <template #footer>
            <el-button @click="drawerFormVisible = false">取 消</el-button>
            <el-button type="primary" @click="updateIdentity">确 定</el-button>
        </template>
    </el-drawer>
    <!-- 新增条目浮窗，里面有一个表单，表单中有需要填写的信息、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogAddNewFormVisible" title="新增身份组信息">
        <el-form :model="addIdentityForm" :rules="rule" ref="addNewFormRef">
            <el-form-item label="用户组id" :label-width="formLabelWidth" prop="id">
                <el-input v-model="addIdentityForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户组名称" :label-width="formLabelWidth" prop="name">
                <el-input v-model="addIdentityForm.name" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户组权限" :label-width="formLabelWidth" prop="identity">
                <!-- 将权限分组显示，每组中通过开关设置权限的开关，每行只显示一个权限-->
                <!-- 账号设置组 -->
                <el-form-item label="账号设置" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="查看当前账户信息" :label-width="formLabelItemWidth" prop="user_info">
                            <el-switch v-model="addIdentityForm.userInfo"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="修改当前账户信息" :label-width="formLabelItemWidth" prop="user_edit">
                            <el-switch v-model="addIdentityForm.userEdit"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="修改当前账户密码" :label-width="formLabelItemWidth" prop="user_password">
                            <el-switch v-model="addIdentityForm.userPassword"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="修改当前账户邮箱" :label-width="formLabelItemWidth" prop="user_email">
                            <el-switch v-model="addIdentityForm.userEmail"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="修改当前账户手机号" :label-width="formLabelItemWidth" prop="user_phone">
                            <el-switch v-model="addIdentityForm.userPhone"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 住宿管理组 -->
                <el-form-item label="住宿管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="查看住宿信息" :label-width="formLabelItemWidth" prop="accommodation_info">
                            <el-switch v-model="addIdentityForm.accommodationInfo"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="提交换宿请求" :label-width="formLabelItemWidth" prop="accommodation_apply">
                            <el-switch v-model="addIdentityForm.accommodationApply"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="审核换宿请求（楼）" :label-width="formLabelItemWidth" prop="accommodation_audit">
                            <el-switch v-model="addIdentityForm.accommodationAudit"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="审核换宿请求（校）" :label-width="formLabelItemWidth" prop="accommodation_confirm">
                            <el-switch v-model="addIdentityForm.accommodationConfirm"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 卫生管理组 -->
                <el-form-item label="卫生管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="查看卫生规范条例" :label-width="formLabelItemWidth" prop="inspect_rules">
                            <el-switch v-model="addIdentityForm.inspectRules"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="宿舍卫生评级" :label-width="formLabelItemWidth" prop="inspect_score">
                            <el-switch v-model="addIdentityForm.inspectScore"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="卫生评分" :label-width="formLabelItemWidth" prop="inspect_mark">
                            <el-switch v-model="addIdentityForm.inspectMark"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="卫生活动管理" :label-width="formLabelItemWidth" prop="inspect_activity">
                            <el-switch v-model="addIdentityForm.inspectActivity"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"
                            />
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 报修管理组 -->
                <el-form-item label="报修管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="提交报修" :label-width="formLabelItemWidth" prop="maintenance_apply">
                            <el-switch v-model="addIdentityForm.maintenanceApply"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="查看报修历史" :label-width="formLabelItemWidth" prop="maintenance_history">
                            <el-switch v-model="addIdentityForm.maintenanceHistory"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="查看楼栋报修信息" :label-width="formLabelItemWidth"
                                      prop="maintenance_building">
                            <el-switch v-model="addIdentityForm.maintenanceBuilding"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="报修管理" :label-width="formLabelItemWidth" prop="maintenance_manage">
                            <el-switch v-model="addIdentityForm.maintenanceManage"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 缴费管理组 -->
                <el-form-item label="缴费管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="水电缴费" :label-width="formLabelItemWidth" prop="payment_bill">
                            <el-switch v-model="addIdentityForm.paymentBill"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="查看历史缴费" :label-width="formLabelItemWidth" prop="payment_history">
                            <el-switch v-model="addIdentityForm.paymentHistory"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="查看楼栋缴费信息" :label-width="formLabelItemWidth" prop="payment_building">
                            <el-switch v-model="addIdentityForm.paymentBuilding"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="缴费管理" :label-width="formLabelItemWidth" prop="payment_manage">
                            <el-switch v-model="addIdentityForm.paymentManage"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 饮用水管理组 -->
                <el-form-item label="饮用水管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="饮用水订购" :label-width="formLabelItemWidth" prop="water_bill">
                            <el-switch v-model="addIdentityForm.waterBill"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="查看订购记录" :label-width="formLabelItemWidth" prop="water_history">
                            <el-switch v-model="addIdentityForm.waterHistory"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="订购管理（送水）" :label-width="formLabelItemWidth" prop="water_sending">
                            <el-switch v-model="addIdentityForm.waterSending"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="订购管理（信息）" :label-width="formLabelItemWidth" prop="water_manage">
                            <el-switch v-model="addIdentityForm.waterManage"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 系统管理组 -->
                <el-form-item label="系统管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="用户组权限管理" :label-width="formLabelItemWidth" prop="system_permission">
                            <el-switch v-model="addIdentityForm.systemPermission"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="用户所属组管理" :label-width="formLabelItemWidth" prop="system_assignment">
                            <el-switch v-model="addIdentityForm.systemAssignment"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
                <!-- 数据管理组 -->
                <el-form-item label="数据管理" :label-width="formLabelWidth">
                    <el-row>
                        <el-form-item label="管理账号数据" :label-width="formLabelItemWidth" prop="manage_account">
                            <el-switch v-model="addIdentityForm.manageAccount"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="管理学生数据" :label-width="formLabelItemWidth" prop="manage_student">
                            <el-switch v-model="addIdentityForm.manageStudent"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="管理职工数据" :label-width="formLabelItemWidth" prop="manage_staff">
                            <el-switch v-model="addIdentityForm.manageStaff"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                    <el-row>
                        <el-form-item label="管理住宿数据" :label-width="formLabelItemWidth" prop="manage_accommodation">
                            <el-switch v-model="addIdentityForm.manageAccommodation"
                                       inline-prompt
                                       :active-text="permitText"
                                       :inactive-text="refuseText"
                                       :active-value="true"
                                       :inactive-value="false"/>
                        </el-form-item>
                    </el-row>
                </el-form-item>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
                <el-button type="primary" @click="createNewIdentity">确认</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>