<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils"
import {post, getStudentInfo, managerDeleteStudent} from "@/net";
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
// let userGroup = getUserGroup();

// document.title = "学生数据管理" + " - HMS(" + userGroup + ")";

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
    document.title = "学生数据管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const changeInfoForm = reactive({
    id: '',
    name: '',
    gender: '',
    email: '',
    phoneNum: '',
    dept: '',
    major: '',
    classId: '',
    admission: 0,
    graduation: 0
})

const addStudentForm = reactive({
    id: '',
    name: '',
    gender: '',
    email: '',
    phoneNum: '',
    dept: '',
    major: '',
    classId: '',
    admission: new Date().getFullYear(),
    graduation: new Date().getFullYear() + 4
})

const validateId = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请输入学号"))
    }
    else if(!/^\d{9}$/.test(value)){
        callback(new Error("学号格式有误"))
    } else {
        callback()
    }
}

const rule = {
    id: [
        {required: true, message: '请输入学号', trigger: 'blur'},
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
    dept:[
        {required: true, message: '请输入院系', trigger: 'blur'}
    ],
    major:[
        {required: true, message: '请输入专业', trigger: 'blur'}
    ],
    classId: [
        {required: true, message: '请输入班级号', trigger: 'blur'}
    ],
    admission: [
        {required: true, message: '请输入入学年份', trigger: 'blur'}
    ],
    graduation: [
        {required: true, message: '请输入毕业年份', trigger: 'blur'}
    ],
}

const newValidateId = (rule, value, callback) => {
    if (!/^\d{9}$/.test(value)) {
        callback(new Error("学号格式有误"))
    }  else {
        callback()
    }
}

const newStudentRule = {
    id: [
        {required: true, message: '请输入学号', trigger: 'blur'},
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
    dept: [
        {required: true, message: '请输入院系', trigger: 'blur'}
    ],
    major: [
        {required: true, message: '请输入专业', trigger: 'blur'}
    ],
    classId: [
        {required: true, message: '请输入班级号', trigger: 'blur'}
    ],
    admission: [
        {required: true, message: '请输入入学年份', trigger: 'blur'}
    ],
    graduation: [
        {required: true, message: '请输入毕业年份', trigger: 'blur'}
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


const toggleSelection = (rows?: Student[]) => {
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

const selectionRows = (rows?: Student[]) => {
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

interface Student {
    id: String,
    name: String,
    gender: String,
    email: String,
    phoneNum: String,
    dept: String,
    major: String,
    classId: String,
    admission: number,
    graduation: number,
}

const form = reactive({
    id: '',
    name: '',
    email: '',
    phoneNum: '',
    dept: '',
    major: '',
    classId: '',
})

// 将getStudentInfo封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    getStudentInfo(currentPage.value, pageSize.value, {...form}, (data) => {
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

// 删除学生
function deleteStudent(row: Student) {
    console.log("deleteStudent:" + row.id);
    createDialog("删除学生", "确认删除学生" + row.id + "吗？").then((res) => {
        if (res){
            managerDeleteStudent(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("学生" + row.id + "删除成功");
                updateInfoTable();
            }, (error) => {
                console.error("删除学生失败", error);
                ElMessage.error("学生" + row.id + "删除失败");
            });
        }
    }, reason => {
        console.log("取消删除学生", reason);
    });
}

// 批量删除学生
function deleteStudents(){

}

// 编辑学生浮窗
function editStudent(row: Student) {
    console.log("editStudent:" + row.id);
    changeInfoForm.id = row.id;
    changeInfoForm.name = row.name;
    changeInfoForm.gender = row.gender;
    changeInfoForm.email = row.email;
    changeInfoForm.phoneNum = row.phoneNum;
    changeInfoForm.dept = row.dept;
    changeInfoForm.major = row.major;
    changeInfoForm.classId = row.classId;
    changeInfoForm.admission = row.admission;
    changeInfoForm.graduation = row.graduation;
    dialogFormVisible.value = true;
}

// 新增学生浮窗
function addNewStudent() {
    dialogAddNewFormVisible.value = true;
}

async function isFormValid(){
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 等待对话框关闭后，更新表格
function updateStudent() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/student/edit', {...changeInfoForm}, (data) => {
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

// 等待增加学生对话框关闭后，更新表格
function createNewStudent() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/student/add', {...addStudentForm}, (data) => {
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
                        <el-input v-model="form.id" placeholder="学号" style="width: 100px; text-align: center"/>
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
                    <el-form-item label="">
                        <el-input v-model="form.dept" placeholder="院系" style="width: 100px"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.major" placeholder="专业" style="width: 100px"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.classId" placeholder="班级号" style="width: 100px"/>
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
                <el-button type="primary" size="small" :icon="CirclePlus" @click="addNewStudent">添加学生</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="" disabled>下载模板文件</el-button>
                <el-button type="primary" size="small" :icon="Upload" @click="" disabled>上传批量添加</el-button>
                <el-button type="danger" size="small" :icon="Delete" @click="deleteStudents" disabled>批量删除</el-button>
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
            <el-table-column prop="id" label="学号" min-width="140" sortable align="center">
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
            <el-table-column prop="dept" label="院系" min-width="240" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.dept)">{{ row.dept }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="major" label="专业" min-width="200" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.major)">{{ row.major }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="classId" label="班级号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.classId)">{{ row.classId }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="admission" label="入学年份" min-width="120" sortable align="center" />
            <el-table-column prop="graduation" label="毕业年份" min-width="120" sortable align="center" />
            <!-- 操作列，有两个按钮：编辑和删除，点击编辑按钮跳弹出编辑学生浮窗，点击删除按钮弹出确认删除提示窗，然后删除学生 -->
            <el-table-column prop="id" label="操作" fixed="right" width="200" align="center">
                <template #default="{row}">
                    <el-button type="warning" size="small" :icon="Edit" @click="editStudent(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteStudent(row)">删除</el-button>
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
    <!-- 浮窗，里面有一个表单，表单中有学号/工号、邮箱、密码、学生状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="学生信息修改">
        <el-form :model="changeInfoForm" :rules="rule" ref="formRef">
            <el-form-item label="学号" :label-width="formLabelWidth" prop="id">
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
            <el-form-item label="院系" :label-width="formLabelWidth" prop="dept">
                <el-input v-model="changeInfoForm.dept" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="专业" :label-width="formLabelWidth" prop="major">
                <el-input v-model="changeInfoForm.major" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="班级号" :label-width="formLabelWidth" prop="classId">
                <el-input v-model="changeInfoForm.classId" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="入学年份" :label-width="formLabelWidth" prop="admission">
                <el-input-number v-model="changeInfoForm.admission" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="毕业年份" :label-width="formLabelWidth" prop="graduation">
                <el-input-number v-model="changeInfoForm.graduation" style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateStudent" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
    <!-- 新增信息浮窗，里面有一个表单，表单中有学号/工号、邮箱、密码、学生状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogAddNewFormVisible" title="新增学生信息">
        <el-form :model="addStudentForm" :rules="newStudentRule" ref="addNewFormRef">
            <el-form-item label="学号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="addStudentForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="姓名" :label-width="formLabelWidth" prop="name">
                <el-input v-model="addStudentForm.name" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="性别" :label-width="formLabelWidth" prop="gender">
                <el-select v-model="addStudentForm.gender" style="width: 240px" placeholder="请选择性别" :label-width="formLabelWidth">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item label="电子邮箱" :label-width="formLabelWidth" prop="email">
                <el-input v-model="addStudentForm.email" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="手机号" :label-width="formLabelWidth" prop="phoneNum">
                <el-input v-model="addStudentForm.phoneNum" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="院系" :label-width="formLabelWidth" prop="dept">
                <el-input v-model="addStudentForm.dept" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="专业" :label-width="formLabelWidth" prop="major">
                <el-input v-model="addStudentForm.major" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="班级号" :label-width="formLabelWidth" prop="classId">
                <el-input v-model="addStudentForm.classId" style="width: 240px" autocomplete="off"/>
                <el-button type="primary" style="margin-left: 10px" @click="addStudentForm.classId = addStudentForm.id.substring(0, 7)">提取班级号</el-button>
            </el-form-item>
            <el-form-item label="入学年份" :label-width="formLabelWidth" prop="admission">
                <el-input-number v-model="addStudentForm.admission" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="毕业年份" :label-width="formLabelWidth" prop="graduation">
                <el-input-number v-model="addStudentForm.graduation" style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewStudent" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>