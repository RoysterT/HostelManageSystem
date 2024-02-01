<script lang="ts" setup>
import {reactive, ref} from "vue";
import {
    post, getAccountInfo, getIdentityList, managerResetPassword,
    managerChangeAccountStatus, managerDeleteAccount
} from "@/net";
import {getUserGroup} from "@/views/scripts/UserUtils";
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
import {getUserIdentity} from "@/views/scripts/UserUtils";
import {ElMessageBox} from 'element-plus'

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const small = ref(false);
const total = ref(0);
const background = ref(true);
const pageSizes = ref([1, 2, 10, 20, 30, 50, 100]);
let dialogFormVisible = ref(false);
const formLabelWidth = '140px';
const loading = ref(false);
const multipleTableRef = ref<InstanceType<typeof ElTable>>();
const formRef = ref();
let dialogAddNewFormVisible = ref(false);
const addNewFormRef = ref();

let userGroup = ref("");

const mainTableVisable = ref(false);
const identityList = ref([]);
const filtersList = ref([]);

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
    document.title = "账号数据管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const form = reactive({
    id: '',
    email: ''
})

const changeInfoForm = reactive({
    regOrder: 0,
    id: '',
    email: '',
    identity: 0
})

const addAccountForm = reactive({
    id: '',
    email: '',
    password: '',
    status: true,
    identity: 3
})

const validateId = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请输入学号或工号"))
    } else if (changeInfoForm.identity == 3 && !/^\d{9}$/.test(value)) {
        callback(new Error("学号格式有误"))
    } else if (changeInfoForm.identity != 3 && !/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    } else if(changeInfoForm.id.length!=6 && changeInfoForm.id.length!=9){
        callback(new Error("学号或工号格式有误"))
    }else {
        callback()
    }
}

const rule = {
    id: [
        {required: true, message: '请输入学号 / 工号', trigger: 'blur'},
        {validator: validateId, trigger: ['blur', 'change']}
    ],
    email: [
        {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
    ],
    identity: [
        {required: true, message: '请选择用户组', trigger: 'blur'}
    ]
}

const newValidateId = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请输入学号或工号"))
    } else if (addAccountForm.identity == 3 && !/^\d{9}$/.test(value)) {
        callback(new Error("学号格式有误"))
    } else if (addAccountForm.identity != 3 && !/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    } else {
        callback()
    }
}

const newAccountRule = {
    id: [
        {required: true, message: '请输入学号 / 工号', trigger: 'blur'},
        {validator: newValidateId, trigger: ['blur', 'change']}
    ],
    email: [
        {required: true, message: '请输入电子邮箱地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮箱地址', trigger: ['blur', 'change']}
    ],
    password: [
        {required: true, message: '请输入密码', trigger: 'blur'},
        {min: 6, max: 50, message: '密码长度在6到50个字符', trigger: 'blur'}
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


const toggleSelection = (rows?: Account[]) => {
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

const selectionRows = (rows?: Account[]) => {
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

interface Account {
    regOrder: number;
    id: string;
    email: string;
    password: string;
    status: boolean;
    identity: number;
}

interface newAccount {
    id: string;
    email: string;
    password: string;
    status: boolean;
    identity: number;
}

const identityFilterTag = (value: number, row: Account) => {
    return row.identity === value
}

const statusFilterTag = (value: boolean, row: Account) => {
    return row.status === value
}

// 将getAccountInfo封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    getAccountInfo(currentPage.value, pageSize.value, {...form}, (data) => {
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

// 重置密码
function resetPassword(row: Account) {
    console.log("resetPassword:" + row.id);
    createDialog("重置账户密码", "确认重置账户" + row.id + "的密码吗？").then((res) => {
        if (res){
            managerResetPassword(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("账户" + row.id + "密码重置成功");
            }, (error) => {
                console.error("重置密码失败", error);
                ElMessage.error("账户" + row.id + "密码重置失败");
            });
        }
    }, reason => {
        console.log("取消重置密码");
    });
}

// 转换账户状态
function changeAccountStatus(row: Account) {
    console.log("changeAccountStatus:" + row.id);
    const status = !row.status == true ? "正常" : "禁用";
    createDialog("更改账户状态", "确定转换账户" + row.id + "的状态为" + status + "吗？").then((res) => {
        if (res){
            managerChangeAccountStatus(row.id, !row.status, (data) => {
                console.log("data:", data);
                ElMessage.success("账户" + row.id + "状态转换成功");
                updateInfoTable();
            }, (error) => {
                console.error("转换账户状态失败", error);
                ElMessage.error("账户" + row.id + "状态转换失败");
            });
        }
    }, reason => {
        console.log("取消转换账户状态");
    });
}

// 删除账户
function deleteAccount(row: Account) {
    console.log("deleteAccount:" + row.id);
    createDialog("删除账户", "确认删除账户" + row.id + "吗？").then((res) => {
        if (res){
            managerDeleteAccount(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("账户" + row.id + "删除成功");
                updateInfoTable();
            }, (error) => {
                console.error("删除账户失败", error);
                ElMessage.error("账户" + row.id + "删除失败");
            });
        }
    }, reason => {
        console.log("取消删除账户");
    });
}

// 批量删除账户
function deleteAccounts() {

}

// 编辑账户浮窗
function editAccount(row: Account) {
    console.log("editAccount:" + row.id);
    changeInfoForm.regOrder = row.regOrder;
    changeInfoForm.id = row.id;
    changeInfoForm.email = row.email;
    changeInfoForm.identity = row.identity;
    dialogFormVisible.value = true;
}

// 新增账户浮窗
function addNewAccount() {
    dialogAddNewFormVisible.value = true;
}

async function isFormValid() {
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 等待更新账户信息对话框关闭后，更新表格
function updateAccount() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/account/edit', {...changeInfoForm}, (data) => {
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
function createNewAccount() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/account/add', {...addAccountForm}, (data) => {
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
    <!--将从接口/api/manage/account/info获得的json对象的数据部分以列表形式打出-->
    <div class="table-container">
        <div class="table-header">
            <div class="table-header-search-area">
                <el-form :inline="true" :model="form" class="demo-form-inline" ref="formRef">
                    <el-form-item label="">
                        <el-input v-model="form.id" placeholder="学号 / 工号"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.email" placeholder="邮箱"/>
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
                <el-button type="primary" size="small" :icon="CirclePlus" @click="addNewAccount">新增账户</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="" disabled>下载模板文件</el-button>
                <el-button type="primary" size="small" :icon="Upload" @click="" disabled>上传批量添加</el-button>
                <el-button type="danger" size="small" :icon="Delete" @click="deleteAccounts" disabled>批量删除</el-button>
                <el-button type="danger" size="small" :icon="Loading" @click="" disabled>批量重置密码</el-button>
            </div>
        </div>
        <el-table :data="tableData"
                  class-name="table-main"
                  empty-text="未查询到数据！"
                  header-align="center"
                  ref="multipleTableRef"
                  v-if="mainTableVisable"
                  v-loading="loading"
        >
            <el-table-column type="selection" width="80" fixed align="center"/>
            <el-table-column prop="regOrder" label="记录序号" min-width="100" sortable align="center"/>
            <!-- id列，标签名学号/工号，可排序，点击可以一键复制 -->
            <el-table-column prop="id" label="学号/工号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.id)">{{ row.id }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="220" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.email)">{{ row.email }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <!-- 密码列显示“重置”按钮 -->
            <el-table-column prop="password" label="密码" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-button type="primary" size="small" @click="resetPassword(row)">重置</el-button>
                </template>
            </el-table-column>
            <!-- 账户状态列有两种值：true或false，true显示为绿色底色的按钮，false显示为红色底色，按钮点击后弹出确认转换提示窗，然后转换账户状态 -->
            <el-table-column prop="status" label="账户状态" min-width="120" sortable align="center"
                             :filters="[
                                 { text: '正常', value: true },
                                 { text: '禁用', value: false },
                               ]"
                             :filter-method="statusFilterTag">
                <template #default="{row}">
                    <el-button type="success" size="small" v-if="row.status" :icon="Select"
                               @click="changeAccountStatus(row)">正常
                    </el-button>
                    <el-button type="danger" size="small" v-else :icon="CloseBold" @click="changeAccountStatus(row)">
                        禁用
                    </el-button>
                </template>
            </el-table-column>
            <!-- 一个可以筛选的列 -->
            <!-- filters从identityList中提取 -->
            <el-table-column prop="identity"
                             label="用户组"
                             sortable
                             min-width="140"
                             :filters="filtersList"
                             :filter-method="identityFilterTag"
                             v-if="identityList!=null&&identityList.length!=0"
                             align="center">
                <template #default="{row}">
                    <!-- 从identityList中动态显示用户组标签 -->
                    <el-tag>{{ identityList.find(item => item.value === row.identity).label }}</el-tag>
                </template>
            </el-table-column>
            <!-- 操作列，有两个按钮：编辑和删除，点击编辑按钮跳弹出编辑账户浮窗，点击删除按钮弹出确认删除提示窗，然后删除账户 -->
            <el-table-column prop="id" label="操作" width="200" fixed="right" align="center">
                <template #default="{row}">
                    <el-button type="warning" size="small" :icon="Edit" @click="editAccount(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteAccount(row)">删除</el-button>
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
    <!-- 浮窗，里面有一个表单，表单中有学号/工号、邮箱、密码、账户状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="账户信息修改">
        <el-form :model="changeInfoForm" :rules="rule" ref="formRef">
            <el-form-item label="记录编号" :label-width="formLabelWidth" prop="regOrder">
                <el-input v-model="changeInfoForm.regOrder" style="width: 240px" autocomplete="off" disabled/>
            </el-form-item>
            <el-form-item label="学号 / 工号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="changeInfoForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="电子邮箱" :label-width="formLabelWidth" prop="email">
                <el-input v-model="changeInfoForm.email" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户组" :label-width="formLabelWidth" prop="identity">
                <el-select v-model="changeInfoForm.identity" placeholder="请选择用户组"
                           style="width: 240px" :label-width="formLabelWidth">
                    <el-option v-for="item in identityList" :key="item.value" :label="item.label"
                               :value="item.value"/>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateAccount" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
    <!-- 新增账户浮窗，里面有一个表单，表单中有学号/工号、邮箱、密码、账户状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogAddNewFormVisible" title="新增账户信息">
        <el-form-item label="用户组" :label-width="formLabelWidth" prop="identity">
            <el-select v-model="addAccountForm.identity" placeholder="请选择用户组"
                       style="width: 240px" :label-width="formLabelWidth">
                <el-option v-for="item in identityList" :key="item.value" :label="item.label"
                           :value="item.value"/>
            </el-select>
        </el-form-item>
        <el-form :model="addAccountForm" :rules="newAccountRule" ref="addNewFormRef">
            <el-form-item label="学号 / 工号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="addAccountForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="电子邮箱" :label-width="formLabelWidth" prop="email">
                <el-input v-model="addAccountForm.email" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="密码" :label-width="formLabelWidth" prop="password">
                <el-input type="password" v-model="addAccountForm.password" style="width: 240px" autocomplete="off" disabled/>
                <el-button type="primary" style="margin-left: 10px" @click="addAccountForm.password = addAccountForm.id.slice(-6)">生成默认密码</el-button>
            </el-form-item>
            <el-form-item label="账户状态" :label-width="formLabelWidth" prop="status">
                <el-switch v-model="addAccountForm.status" active-color="#13ce66" inactive-color="#ff4949"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewAccount" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
</style>