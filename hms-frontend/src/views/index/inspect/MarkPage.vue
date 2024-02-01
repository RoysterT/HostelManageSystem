<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserId, getUserName, getUserGroup, getManageBuilding} from "@/views/scripts/UserUtils";
import {get, post, managerDeleteHygiene, getHygieneInfo, getActivityArray, getAllActivityArray} from "@/net";
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
const isToConfirm = ref(true);
const activeActivityArray = ref([])
const allActivityArray = ref([])

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
    document.title = "卫生评分" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

getActivityArray((data) => {
    console.log("可选择活动：", data);
    activeActivityArray.value = data;
}, (error) => {
    console.error("获取活动列表失败", error);
});

getAllActivityArray((data) => {
    console.log("全部活动：", data);
    allActivityArray.value = data;
}, (error) => {
    console.error("获取活动列表失败", error);
});

// 高级搜索表单
const searchForm = reactive({
    recordId: 0,
    activityId: 0,
    building: '',
    unit: '',
    room: '',
    progress: 0,
})

const searchRule = {}

const addHygieneForm = reactive({
    recordId: 0,
    activityId: 0,
    building: getManageBuilding(getUserId()),
    unit: '',
    room: '',
    score: 0,
    recordTime: 0,
    description: '',
    progress: 0
})

const addHygieneRule = {
    building: [
        {required: true, message: '请选择楼栋号', trigger: 'change'}
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'change'}
    ],
    room: [
        {required: true, message: '请填写房间号', trigger: 'blur'}
    ],
    score: [
        {required: true, message: '请填写评分', trigger: 'blur'}
    ],
    recordTime: [
        {required: true, message: '请选择评分时间', trigger: 'change'}
    ],
    description: [
        {required: true, message: '请填写评分描述', trigger: 'blur'}
    ],
}

const changeInfoForm = reactive({
    recordId: 0,
    activityId: 0,
    building: '',
    unit: '',
    room: '',
    score: 0,
    recordTime: 0,
    description: '',
    progress: 0
})

const hygieneInfoDrawerData = reactive({
    recordId: 0,
    activityId: 0,
    building: '',
    unit: '',
    room: '',
    score: 0,
    recordTime: '',
    description: '',
    progress: 0
})

const updateRule = {
    building: [
        {required: true, message: '请选择楼栋号', trigger: 'change'}
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'change'}
    ],
    room: [
        {required: true, message: '请填写房间号', trigger: 'blur'}
    ],
    score: [
        {required: true, message: '请填写评分', trigger: 'blur'}
    ],
    recordTime: [
        {required: true, message: '请选择评分时间', trigger: 'change'}
    ],
    description: [
        {required: true, message: '请填写评分描述', trigger: 'blur'}
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

const toggleSelection = (rows?: Hygiene[]) => {
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

const selectionRows = (rows?: Hygiene[]) => {
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

interface Hygiene {
    recordId: number,
    activityId: number,
    building: string,
    unit: string,
    room: string,
    score: number,
    recordTime: number,
    description: string,
    progress: number
}

function updateInfoTable() {
    searchFormVisible.value = false;
    loading.value = true;
    const type = isToConfirm.value ? "confirm" : "all";
    const url = '/api/hygiene/mark/list?page=' + currentPage.value
        + '&pageSize=' + pageSize.value
        + '&type=' + type;
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

// 编辑评分浮窗
function editHygiene(row: Hygiene) {
    console.log("editHygiene:" + row.recordId.valueOf());
    changeInfoForm.recordId = row.recordId;
    changeInfoForm.activityId = row.activityId;
    changeInfoForm.building = row.building;
    changeInfoForm.unit = row.unit;
    changeInfoForm.room = row.room;
    changeInfoForm.score = row.score;
    changeInfoForm.recordTime = row.recordTime;
    changeInfoForm.description = row.description;
    changeInfoForm.progress = row.progress;
    dialogFormVisible.value = true;
}

function deleteHygiene(row: Hygiene) {
    console.log("deleteHygiene:" + row.recordId);
    createDialog("删除确认", "确认删除评分序号为" + row.recordId + "的评分吗？").then((value) => {
        if (value) {
            managerDeleteHygiene(row.recordId, () => {
                ElMessage.success("删除成功！")
                updateInfoTable();
            }, (error) => {
                ElMessage.error(error)
                console.error("删除失败，", error);
            })
        }
    })
}

// 添加评分
function addNewHygiene() {
    addNewFormRef.value.validate((valid) => {
        if (valid) {
            post('/api/hygiene/mark/add', {...addHygieneForm}, (data) => {
                ElMessage.success("评分成功！")
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

// 查看评分详情
function hygieneInfo(row: Hygiene) {
    getHygieneInfo(row.recordId, (data) => {
        hygieneInfoDrawerData.recordId = data.recordId;
        hygieneInfoDrawerData.activityId = data.activityId;
        hygieneInfoDrawerData.building = data.building;
        hygieneInfoDrawerData.unit = data.unit;
        hygieneInfoDrawerData.room = data.room;
        hygieneInfoDrawerData.score = data.score;
        hygieneInfoDrawerData.recordTime = new Date(data.recordTime).toLocaleString();
        hygieneInfoDrawerData.description = data.description;
        hygieneInfoDrawerData.progress = data.progress;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取评分详情失败", error);
    });
}

// 等待对话框关闭后，更新表格
function updateHygiene() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/hygiene/mark/edit', {...changeInfoForm}, () => {
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

function rectifySuccess(row: Hygiene) {
    console.log("rectifySuccess:" + row.recordId);
    get('/api/hygiene/mark/rectify/success/' + row.recordId, () => {
        ElMessage.success("确认成功！")
        updateInfoTable();
    }, (error) => {
        ElMessage.error(error)
        console.error("确认失败，", error);
    })
}

</script>

<template>
    <div class="table-container">
        <div class="table-header">
            <!-- 操作区域 -->
            <div class="table-header-tools-area">
                <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                <el-button type="primary" size="small" :icon="Search" @click="searchFormVisible = true">高级搜索</el-button>
                <el-button type="success" size="small" :icon="CirclePlus" @click="dialogAddNewFormVisible = true">添加评分</el-button>
                <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
                <!-- 切换视图按钮，改变isToConfirm的值，即“切换到全部记录”和”切换到待确认记录“ -->
                <el-button type="primary" size="small" :icon="Refresh"
                           @click="isToConfirm = !isToConfirm;updateInfoTable()">
                    <template v-if="isToConfirm">查看全部记录</template>
                    <template v-else>仅查看待确认记录</template>
                </el-button>
            </div>
            <!-- 高级搜索浮窗 -->
            <el-dialog v-model="searchFormVisible" title="高级搜索">
                <el-form model="searchForm" :rules="searchRule" ref="searchRef">
                    <el-form-item label="记录序号" :label-width="formLabelWidth" prop="recordId">
                        <el-input v-model="searchForm.recordId" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="活动序号" :label-width="formLabelWidth" prop="activityId">
                        <el-select v-model="searchForm.activityId" :label-width="formLabelItemWidth"
                                   placeholder="请选择活动序号">
                            <el-option v-for="item in allActivityArray" :key="item.value"
                                       :label="item.label" :value="item.value"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="楼栋" :label-width="formLabelWidth" prop="building">
                        <el-select v-model="searchForm.building" :label-width="formLabelItemWidth"
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
                        <el-select v-model="searchForm.unit" :label-width="formLabelItemWidth"
                                   placeholder="请选择单元号">
                            <el-option label="A单元" value="A"/>
                            <el-option label="B单元" value="B"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="房间" :label-width="formLabelWidth" prop="room">
                        <el-input v-model="searchForm.room" style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="进度" :label-width="formLabelWidth" prop="progress">
                        <el-select v-model="searchForm.progress" :label-width="formLabelItemWidth"
                                   placeholder="请选择进度">
                            <el-option label="发起检查" value="1"/>
                            <el-option label="学生整改" value="2"/>
                            <el-option label="等待验收" value="3"/>
                            <el-option label="确认完成" value="4"/>
                        </el-select>
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
            <el-dialog v-model="dialogAddNewFormVisible" title="新增评分">
                <el-form :model="addHygieneForm" :rules="addHygieneRule" ref="addNewFormRef" status-icon>
                    <el-form-item label="活动序号" :label-width="formLabelWidth" prop="activityId">
                        <el-select v-model="addHygieneForm.activityId" :label-width="formLabelItemWidth"
                                   placeholder="请选择活动序号">
                            <el-option v-for="item in activeActivityArray" :key="item.value"
                                       :label="item.label" :value="item.value"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="楼栋" :label-width="formLabelWidth" prop="building">
                        <el-select v-model="addHygieneForm.building" :label-width="formLabelItemWidth"
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
                        <el-select v-model="addHygieneForm.unit" :label-width="formLabelItemWidth"
                                   placeholder="请选择单元号">
                            <el-option label="A单元" value="A"/>
                            <el-option label="B单元" value="B"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="房间" :label-width="formLabelWidth" prop="room">
                        <el-input v-model="addHygieneForm.room" style="width: 240px"
                                  maxlength="3" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="评分" :label-width="formLabelWidth" prop="score">
                        <!-- 0~100之间，2位小数 -->
                        <el-input-number v-model="addHygieneForm.score" :min="0" :max="100" :step="0.01"
                                         style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                    <el-form-item label="评分描述" :label-width="formLabelWidth" prop="description">
                        <el-input v-model="addHygieneForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4}"
                                  style="width: 240px" autocomplete="off"/>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
                        <el-button type="primary" @click="addNewHygiene">确认</el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
        <el-table :data="tableData"
                  class-name="table-main"
                  empty-text="无待确认记录！"
                  header-align="center"
                  ref="multipleTableRef"
                  v-loading="loading"
                  v-if="mainTableVisable"
                  style="width: 100%; overflow: scroll"
        >
            <el-table-column type="selection" width="80" fixed align="center"/>
            <el-table-column prop="recordId" label="记录序号" min-width="120" sortable align="center"/>
            <el-table-column prop="activityId" label="活动序号" min-width="120" sortable align="center"/>
            <!-- 活动名称，从全部活动列表中找到对应序号的名称 -->
            <el-table-column prop="activityId" label="活动名称" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(allActivityArray.find(item => item.value === row.activityId).label)">
                            <!-- 从全部活动数组字典中找出activityId与其value对应的，取出label值 -->
                            {{ allActivityArray.find(item => item.value === row.activityId).label }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="building" label="楼栋号" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.building)">
                            {{ row.building }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="unit" label="单元号" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.unit)">
                            {{ row.unit }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="room" label="房间号" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.room)">
                            {{ row.room }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="score" label="评分" min-width="120" sortable align="center"/>
            <!-- 评分时间，转为本地时间 -->
            <el-table-column prop="recordTime" label="评分时间" min-width="200" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(new Date(row.recordTime).toLocaleString())">
                            {{ new Date(row.recordTime).toLocaleString() }}
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" min-width="120" sortable align="center">
                <template #default="{row}">
                    <el-tag v-if="row.progress === 1" type="info">发起检查</el-tag>
                    <el-tag v-else-if="row.progress === 2" type="warning">学生整改</el-tag>
                    <el-tag v-else-if="row.progress === 3" type="success">等待验收</el-tag>
                    <el-tag v-else-if="row.progress === 4" type="success">确认完成</el-tag>
                </template>
            </el-table-column>
            <!-- 操作列 -->
            <el-table-column prop="repairOrder" label="操作" fixed="right" width="200" align="center"
                             v-if="isToConfirm">
                <template #default="{row}">
                    <el-button type="info" size="small" :icon="InfoFilled" @click="hygieneInfo(row)">详情</el-button>
                    <el-button type="success" size="small" :icon="SuccessFilled" @click="rectifySuccess(row)" v-if="row.progress==3">确认</el-button>
                    <el-button type="success" size="small" :icon="SuccessFilled" @click="rectifySuccess(row)" v-else disabled>确认</el-button>
                </template>
            </el-table-column>
            <el-table-column prop="repairOrder" label="操作" fixed="right" width="200" align="center" v-else>
                <template #default="{row}">
                    <el-button type="info" size="small" :icon="InfoFilled" @click="hygieneInfo(row)">详情</el-button>
                    <el-button type="primary" size="small" :icon="InfoFilled" @click="editHygiene(row)">编辑</el-button>
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
    <!-- 评分详情抽屉 -->
    <el-drawer v-model="infoDrawerVisible"
               title="评分详情"
               :direction="'rtl'"
               :with-header="true"
               size="30%"
    >
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">评分序号[ {{ hygieneInfoDrawerData.recordId }} ]的详情信息</h4>
        </template>
        <!-- 使用描述列表el-descriptions显示内容，字段全部可以可以一键复制 -->
        <el-descriptions :column="1" border>
            <el-descriptions-item label="记录序号">{{ hygieneInfoDrawerData.recordId }}</el-descriptions-item>
            <el-descriptions-item label="活动序号">{{ hygieneInfoDrawerData.activityId }}</el-descriptions-item>
            <el-descriptions-item label="活动名称">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(allActivityArray.find(item => item.value === hygieneInfoDrawerData.activityId).label)">
                        {{ allActivityArray.find(item => item.value === hygieneInfoDrawerData.activityId).label }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="楼栋号">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(hygieneInfoDrawerData.building)">
                        {{ hygieneInfoDrawerData.building }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="单元号">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(hygieneInfoDrawerData.unit)">
                        {{ hygieneInfoDrawerData.unit }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="房间号">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(hygieneInfoDrawerData.room)">
                        {{ hygieneInfoDrawerData.room }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="评分">{{ hygieneInfoDrawerData.score }}</el-descriptions-item>
            <el-descriptions-item label="评分时间">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(hygieneInfoDrawerData.recordTime)">
                        {{ hygieneInfoDrawerData.recordTime }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="评分描述">
                <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                    <el-button type="text" @click="copy(hygieneInfoDrawerData.description)">
                        {{ hygieneInfoDrawerData.description }}
                    </el-button>
                </el-tooltip>
            </el-descriptions-item>
            <el-descriptions-item label="进度">
                <template #default>
                    <el-steps :active="hygieneInfoDrawerData.progress<4?hygieneInfoDrawerData.progress-1:4"
                              finish-status="success"
                              align-center
                              direction="vertical"
                    >
                        <el-step title="发起检查"/>
                        <el-step title="学生整改"/>
                        <el-step title="等待验收"/>
                        <el-step title="确认完成"/>
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
    <el-dialog v-model="dialogFormVisible" title="评分信息修改">
        <el-form :model="changeInfoForm" :rules="updateRule" ref="formRef">
            <el-form-item label="记录序号"
                          :label-width="formLabelWidth"
                          prop="recordId">
                <el-input v-model="changeInfoForm.recordId"
                          style="width: 240px"
                          autocomplete="off"
                          disabled/>
            </el-form-item>
            <el-form-item label="活动序号"
                          :label-width="formLabelWidth"
                          prop="activityId">
                <el-select v-model="changeInfoForm.activityId"
                           :label-width="formLabelItemWidth"
                           placeholder="请选择活动序号">
                    <el-option v-for="item in activeActivityArray"
                               :key="item.value"
                               :label="item.label"
                               :value="item.value"/>
                </el-select>
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
            <el-form-item label="评分" :label-width="formLabelWidth" prop="score">
                <!-- 0~100之间，2位小数 -->
                <el-input-number v-model="changeInfoForm.score" :min="0" :max="100" :step="0.01"
                                 style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="评分描述" :label-width="formLabelWidth" prop="description">
                <el-input v-model="changeInfoForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4}"
                          style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
            <el-button @click="isFormValid">检查</el-button>
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="updateHygiene" :disabled="!isFormValid">确认</el-button>
        </span>
        </template>
    </el-dialog>
</template>


<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>