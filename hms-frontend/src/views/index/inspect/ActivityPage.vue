<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserName, getUserGroup} from "@/views/scripts/UserUtils";
import {get, post, managerDeleteActivity, getActivityInfo} from "@/net";
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
    document.title = "卫生活动管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const addActivityForm = reactive({
    activityId: 0,
    name: '',
    description: '',
    timeRange: [],
})

const addActivityRule = {
    name: [
        {required: true, message: '请输入活动名称', trigger: 'blur'},
        {min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '请输入活动描述', trigger: 'blur'},
        {min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur'}
    ],
    timeRange: [
        {type: 'array', required: true, message: '请选择活动时间', trigger: 'change'}
    ]
}

const changeActivityForm = reactive({
    activityId: 0,
    name: '',
    description: '',
    timeRange: [],
})

const activityInfoDrawerData = reactive({
    activityId: 0,
    name: '',
    description: '',
    startTime: 0,
    endTime: 0,
})

const updateRule = {
    name: [
        {required: true, message: '请输入活动名称', trigger: 'blur'},
        {min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur'}
    ],
    description: [
        {required: true, message: '请输入活动描述', trigger: 'blur'},
        {min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur'}
    ],
    timeRange: [
        {type: 'array', required: true, message: '请选择活动时间', trigger: 'change'}
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

const toggleSelection = (rows?: Activity[]) => {
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

const selectionRows = (rows?: Activity[]) => {
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

interface Activity {
    activityId: number,
    name: string,
    description: string,
    startTime: number,
    endTime: number
}

// 将getActivityInfo封装为一个函数，方便调用
function updateInfoTable() {
    searchFormVisible.value = false;
    loading.value = true;
    const url = '/api/hygiene/activity/all/list?page=' + currentPage.value + '&pageSize=' + pageSize.value;
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

// 编辑活动浮窗
function editActivity(row: Activity) {
    console.log("editActivity:" + row.activityId);
    changeActivityForm.activityId = row.activityId;
    changeActivityForm.name = row.name;
    changeActivityForm.description = row.description;
    changeActivityForm.timeRange = [new Date(row.startTime), new Date(row.endTime)];
    dialogFormVisible.value = true;
}

function deleteActivity(row: Activity) {
    console.log("deleteActivity:" + row.activityId);
    createDialog("删除确认", "确认删除活动序号为" + row.activityId + "的活动吗？").then((value) => {
        if (value) {
            managerDeleteActivity(row.activityId, () => {
                ElMessage.success("删除成功！")
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("删除失败，", error);
            })
        }
    })
}

// 添加活动
function addNewActivity() {
    addNewFormRef.value.validate((valid) => {
        if (valid) {
            post('/api/hygiene/activity/add', {...addActivityForm}, (data) => {
                ElMessage.success("活动创建成功！")
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

// 查看活动详情
function hygieneInfo(row: Activity) {
    getActivityInfo(row.activityId, (data) => {
        console.log(data)
        activityInfoDrawerData.activityId = data.activityId;
        activityInfoDrawerData.name = data.name;
        activityInfoDrawerData.description = data.description;
        activityInfoDrawerData.startTime = data.startTime;
        activityInfoDrawerData.endTime = data.endTime;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取活动详情失败", error);
    });
}

// 等待对话框关闭后，更新表格
function updateActivity() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/hygiene/activity/edit', {...changeActivityForm}, () => {
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
                <el-button type="primary" size="small" :icon="CirclePlus" @click="dialogAddNewFormVisible = true">
                    添加活动
                </el-button>
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
            </div>
            <!-- 新增条目浮窗，里面有一个表单，表单中有需要填写的信息、确认按钮和取消按钮 -->
            <el-dialog v-model="dialogAddNewFormVisible" title="新增活动">
                <el-form :model="addActivityForm" :rules="addActivityRule" ref="addNewFormRef" status-icon>
                    <el-form-item label="活动名称"
                                  :label-width="formLabelWidth"
                                  prop="name">
                        <el-input v-model="addActivityForm.name"
                                  style="width: 240px"
                                  autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="活动描述"
                                  :label-width="formLabelWidth"
                                  prop="description">
                        <el-input v-model="addActivityForm.description"
                                  style="width: 240px"
                                  autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="活动时间"
                                  :label-width="formLabelWidth"
                                  prop="timeRange">
                        <el-date-picker
                            v-model="addActivityForm.timeRange"
                            type="datetimerange"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            align="right"
                            style="width: 240px"
                            autocomplete="off"
                        />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
                        <el-button type="primary" @click="addNewActivity">确认</el-button>
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
            <el-table-column prop="activityId" label="活动序号" min-width="120" sortable align="center"/>
            <el-table-column prop="name" label="活动名称" min-width="200" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.name)">
                            {{ row.name }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="description" label="活动描述" min-width="200" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.description)">
                            {{ row.description }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" min-width="220" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(new Date(row.startTime).toLocaleString())">
                            {{ new Date(row.startTime).toLocaleString() }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" min-width="220" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(new Date(row.endTime).toLocaleString())">
                            {{ new Date(row.endTime).toLocaleString() }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <!-- 操作列 -->
            <el-table-column prop="hygieneOrder" label="操作" fixed="right" width="300" align="center">
                <template #default="{row}">
                    <el-button type="info" size="small" :icon="InfoFilled" @click="hygieneInfo(row)">详情</el-button>
                    <el-button type="primary" size="small" :icon="Edit" @click="editActivity(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteActivity(row)">删除</el-button>
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
    <!-- 活动详情抽屉 -->
    <el-drawer v-model="infoDrawerVisible"
               title="活动详情"
               :direction="'rtl'"
               :with-header="true"
               size="30%"
    >
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">活动序号[ {{ activityInfoDrawerData.activityId }} ]的详情信息</h4>
        </template>
        <!-- 使用描述列表el-descriptions显示内容，字段全部可以可以一键复制 -->
        <el-descriptions :column="1" border>
            <el-descriptions-item label="活动序号">{{ activityInfoDrawerData.activityId }}</el-descriptions-item>
            <el-descriptions-item label="活动名称">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(activityInfoDrawerData.name)">
                        {{ activityInfoDrawerData.name }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="活动描述">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(activityInfoDrawerData.description)">
                        {{ activityInfoDrawerData.description }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(new Date(activityInfoDrawerData.startTime).toLocaleString())">
                        {{ new Date(activityInfoDrawerData.startTime).toLocaleString() }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(new Date(activityInfoDrawerData.endTime).toLocaleString())">
                        {{ new Date(activityInfoDrawerData.endTime).toLocaleString() }}
                    </el-button>
                </el-tooltip>
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
    <el-dialog v-model="dialogFormVisible" title="活动信息修改">
        <el-form :model="changeActivityForm" :rules="updateRule" ref="formRef">
            <el-form-item label="活动序号"
                          :label-width="formLabelWidth"
                          prop="hygieneId">
                <el-input v-model="changeActivityForm.activityId"
                          style="width: 240px"
                          autocomplete="off"
                          disabled/>
            </el-form-item>
            <el-form-item label="活动名称"
                          :label-width="formLabelWidth"
                          prop="name">
                <el-input v-model="changeActivityForm.name"
                          style="width: 240px"
                          autocomplete="off"/>
            </el-form-item>
            <el-form-item label="活动描述"
                          :label-width="formLabelWidth"
                          prop="description">
                <el-input type="textarea"
                          v-model="changeActivityForm.description"
                          style="width: 240px"
                          autocomplete="off"/>
            </el-form-item>
            <el-form-item label="活动时间"
                          :label-width="formLabelWidth"
                          prop="timeRange">
                <el-date-picker
                    v-model="changeActivityForm.timeRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    align="right"
                    style="width: 240px"
                    autocomplete="off"
                />
            </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
            <el-button @click="isFormValid">检查</el-button>
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="updateActivity" :disabled="!isFormValid">确认</el-button>
        </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>