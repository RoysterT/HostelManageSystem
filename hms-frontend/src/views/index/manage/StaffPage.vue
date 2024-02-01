<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {post, getStaffInfo, managerDeleteStaff, getIdentityList} from "@/net";
import {ElMessage, ElTable} from "element-plus";
import {
    CirclePlus,
    CloseBold,
    Delete,
    Download,
    Edit,
    Loading,
    Refresh,
    Search,
    Select,
    Upload
} from "@element-plus/icons-vue";
import {copy} from "@/views/scripts/ManageUtils";
import {ElMessageBox} from 'element-plus'

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const small = ref(false);
const total = ref(0);
const background = ref(true);
const pageSizes = ref([1, 2, 10, 20, 30, 50, 100]);
let dialogFormVisible = ref(false)
const formLabelWidth = '140px'
const loading = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const formRef = ref()
let dialogAddNewFormVisible = ref(false)
const addNewFormRef = ref()

import {getUserIdentity} from "@/views/scripts/UserUtils";
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
    document.title = "职工数据管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

document.title = "职工数据管理" + " - HMS(" + userGroup + ")";

const changeInfoForm = reactive({
    id: '',
    name: '',
    gender: '',
    email: '',
    phoneNum: '',
    manageBuilding: '',
    identity: 0
})

const addStaffForm = reactive({
    id: '',
    name: '',
    gender: '',
    email: '',
    phoneNum: '',
    manageBuilding: '',
    identity: 0
})

const identityFilterTag = (value: number, row: Staff) => {
    return row.identity === value
}

const manageBuildingFilterTag = (value: string, row: Staff) => {
    return row.manageBuilding === value
}

const validateId = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请输入学号或工号"))
    }
    else if (!/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    } else {
        callback()
    }
}

const rule = {
    id: [
        {required: true, message: '请输入工号', trigger: 'blur'},
        {validator: validateId, trigger: ['blur', 'change']}
    ],
    name: [
        {required: true, message: '请输入姓名', trigger: 'blur'}
    ],
    gender:[
        {required: true, message: '请选择性别', trigger: 'blur'}
    ],
    email: [
        {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
    ],
    phoneNum: [
        {type: 'phone', message: '请输入合法的手机号码', trigger: ['blur', 'change']}
    ],
    identity: [
        {required: true, message: '请选择身份组', trigger: 'blur'}
    ],
}

const newValidateId = (rule, value, callback) => {
    if (!/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    } else {
        callback()
    }
}

const newStaffRule = {
    id: [
        {required: true, message: '请输入工号', trigger: 'blur'},
        {validator: newValidateId, trigger: ['blur', 'change']}
    ],
    name: [
        {required: true, message: '请输入姓名', trigger: 'blur'}
    ],
    gender: [
        {required: true, message: '请选择性别', trigger: 'blur'}
    ],
    email: [
        {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
    ],
    phoneNum: [
        {type: 'phone', message: '请输入合法的手机号码', trigger: ['blur', 'change']}
    ],
    identity: [
        {required: true, message: '请选择用户组', trigger: 'blur'}
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


const toggleSelection = (rows?: Staff[]) => {
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

const selectionRows = (rows?: Staff[]) => {
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

interface Staff {
    id: String,
    name: String,
    gender: String,
    email: String,
    phoneNum: String,
    manageBuilding: String,
    identity:  number,
}

const form = reactive({
    id: '',
    name: '',
    email: '',
    phoneNum: ''
})

// 将getStaffInfo封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    getStaffInfo(currentPage.value, pageSize.value, {...form}, (data) => {
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

// 删除职工
function deleteStaff(row: Staff) {
    console.log("deleteStaff:" + row.id);
    createDialog("删除数据", "确认删除职工" + row.id + "吗？").then((res) => {
        if (res){
            managerDeleteStaff(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("职工" + row.id + "删除成功");
                updateInfoTable();
            }, (error) => {
                console.error("删除职工失败", error);
                ElMessage.error("职工" + row.id + "删除失败");
            });
        }
    }, reason => {
        console.log("取消删除职工" + row.id);
    });
}

// 批量删除职工
function deleteStaffs(){

}

// 编辑职工浮窗
function editStaff(row: Staff) {
    console.log("editStaff:" + row.id);
    changeInfoForm.id = row.id;
    changeInfoForm.name = row.name;
    changeInfoForm.gender = row.gender;
    changeInfoForm.email = row.email;
    changeInfoForm.phoneNum = row.phoneNum;
    changeInfoForm.manageBuilding = row.manageBuilding;
    changeInfoForm.identity = row.identity;
    dialogFormVisible.value = true;
}

// 新增职工浮窗
function addNewStaff() {
    dialogAddNewFormVisible.value = true;
}

async function isFormValid(){
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 等待对话框关闭后，更新表格
function updateStaff() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/staff/edit', {...changeInfoForm}, (data) => {
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

// 等待增加账户对话框关闭后，更新表格
function createNewStaff() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/staff/add', {...addStaffForm}, (data) => {
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
            <div class="table-header-search-area">
                <el-form :inline="true" :model="form" class="demo-form-inline" ref="formRef">
                    <el-form-item label="">
                        <el-input v-model="form.id" placeholder="工号" style="width: 100px; text-align: center"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.name" placeholder="姓名" style="width: 100px"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.email" placeholder="邮箱" style="width: 100px"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.phoneNum" placeholder="手机号" style="width: 100px"/>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" :icon="Search" @click="updateInfoTable">查询</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
                <el-button type="primary" size="small" :icon="CirclePlus" @click="addNewStaff">添加职工</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="" disabled>下载模板文件</el-button>
                <el-button type="primary" size="small" :icon="Upload" @click="" disabled>上传批量添加</el-button>
                <el-button type="danger" size="small" :icon="Delete" @click="deleteStaffs" disabled>批量删除</el-button>
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
            <el-table-column prop="id" label="工号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.id)">{{ row.id }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="name" label="姓名" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.name)">{{ row.name }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="gender" label="性别" min-width="80" sortable align="center"/>
            <el-table-column prop="email" label="邮箱" min-width="220" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.email)">{{ row.email }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="phoneNum" label="手机号" min-width="160" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.phoneNum)">{{ row.phoneNum }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="manageBuilding"
                             label="管理楼栋"
                             min-width="140"
                             sortable
                             :filters="[
                                 { text: '01栋', value: '01' },
                                 { text: '02栋', value: '02' },
                                 { text: '03栋', value: '03' },
                                 { text: '04栋', value: '04' },
                                 { text: '05栋', value: '05' },
                                 { text: '06栋', value: '06' },
                                 { text: '07栋', value: '07' },
                                 { text: '08栋', value: '08' },
                                 { text: '09栋', value: '09' },
                                 { text: '10栋', value: '10' },
                                 { text: '11栋', value: '11' },
                                 { text: '12栋', value: '12' },
                                 { text: '13栋', value: '13' },
                                 { text: '14栋', value: '14' },
                                 { text: '15栋', value: '15' },
                                 { text: '16栋', value: '16' },
                                 { text: '17栋', value: '17' },
                                 { text: '18栋', value: '18' },
                                 { text: '19栋', value: '19' },
                                 { text: '20栋', value: '20' },
                                 { text: '21栋', value: '21' },
                                 { text: '22栋', value: '22' },
                                 { text: '23栋', value: '23' },
                                 { text: '24栋', value: '24' },
                                 { text: '25栋', value: '25' },
                                 { text: '26栋', value: '26' },
                                 { text: '27栋', value: '27' },
                                 { text: '28栋', value: '28' },
                                 { text: '29栋', value: '29' },
                                 { text: '30栋', value: '30' },
                               ]"
                             :filter-method="manageBuildingFilterTag"
                             align="center">
                <template #default="{row}">
                    <el-tag v-if='row.manageBuilding === "01"'>01栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "02"'>02栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "03"'>03栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "04"'>04栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "05"'>05栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "06"'>06栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "07"'>07栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "08"'>08栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "09"'>09栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "10"'>10栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "11"'>11栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "12"'>12栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "13"'>13栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "14"'>14栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "15"'>15栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "16"'>16栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "17"'>17栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "18"'>18栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "19"'>19栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "20"'>20栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "21"'>21栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "22"'>22栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "23"'>23栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "24"'>24栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "25"'>25栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "26"'>26栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "27"'>27栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "28"'>28栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "29"'>29栋</el-tag>
                    <el-tag v-if='row.manageBuilding === "30"'>30栋</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="identity"
                             label="用户组"
                             sortable
                             min-width="140"
                             :filters="filtersList"
                             :filter-method="identityFilterTag"
                             align="center">
                <template #default="{row}">
                    <!-- 从identityList动态更新el-tag -->
                    <el-tag>{{ identityList.find(item => item.value === row.identity).label }}</el-tag>
                </template>
            </el-table-column>
            <!-- 操作列，有两个按钮：编辑和删除，点击编辑按钮跳弹出编辑职工浮窗，点击删除按钮弹出确认删除提示窗，然后删除职工 -->
            <el-table-column prop="id" label="操作" fixed="right" width="200" align="center">
                <template #default="{row}">
                    <el-button type="warning" size="small" :icon="Edit" @click="editStaff(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteStaff(row)">删除</el-button>
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
    <!-- 浮窗，里面有一个表单，表单中有工号/工号、邮箱、密码、职工状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="职工信息修改">
        <el-form :model="changeInfoForm" :rules="rule" ref="formRef">
            <el-form-item label="工号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="changeInfoForm.id" style="width: 240px" autocomplete="off" disabled/>
            </el-form-item>
            <el-form-item label="姓名" :label-width="formLabelWidth" prop="name">
                <el-input v-model="changeInfoForm.name" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="性别" :label-width="formLabelWidth" prop="gender">
                <el-select v-model="changeInfoForm.gender" style="width: 240px" placeholder="请选择性别" :label-width="formLabelWidth">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item label="电子邮箱" :label-width="formLabelWidth" prop="email">
                <el-input v-model="changeInfoForm.email" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="手机号" :label-width="formLabelWidth" prop="phoneNum">
                <el-input v-model="changeInfoForm.phoneNum" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="管理楼栋" :label-width="formLabelWidth" prop="manageBuilding">
                <el-input v-model="changeInfoForm.manageBuilding" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户组" :label-width="formLabelWidth" prop="identity">
                <el-select v-model="changeInfoForm.identity" style="width: 240px" placeholder="请选择用户组" :label-width="formLabelWidth">
                    <el-option v-for="item in identityList" :key="item.value" :label="item.label"
                               :value="item.value"/>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
              <el-button @click="isFormValid">检查</el-button>
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="updateStaff" :disabled="!isFormValid">确认</el-button>
          </span>
        </template>
    </el-dialog>
    <!-- 新增条目浮窗，里面有一个表单，表单中有需要填写的信息、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogAddNewFormVisible" title="新增职工信息">
        <el-form :model="addStaffForm" :rules="newStaffRule" ref="addNewFormRef">
            <el-form-item label="用户组" :label-width="formLabelWidth" prop="identity">
                <el-select v-model="addStaffForm.identity" style="width: 240px" placeholder="请选择用户组" :label-width="formLabelWidth">
                    <el-option v-for="item in identityList" :key="item.value" :label="item.label"
                               :value="item.value"/>
                </el-select>
            </el-form-item>
            <el-form-item label="工号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="addStaffForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="姓名" :label-width="formLabelWidth" prop="name">
                <el-input v-model="addStaffForm.name" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="性别" :label-width="formLabelWidth" prop="gender">
                <el-select v-model="addStaffForm.gender" style="width: 240px" placeholder="请选择性别" :label-width="formLabelWidth">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item label="电子邮箱" :label-width="formLabelWidth" prop="email">
                <el-input v-model="addStaffForm.email" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="手机号" :label-width="formLabelWidth" prop="phoneNum">
                <el-input v-model="addStaffForm.phoneNum" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="管理楼栋" :label-width="formLabelWidth" prop="manageBuilding">
                <el-input v-model="addStaffForm.manageBuilding" style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewStaff" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>