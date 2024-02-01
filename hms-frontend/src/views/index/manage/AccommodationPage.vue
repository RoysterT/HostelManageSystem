<script lang="ts" setup>
import {reactive, ref} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {post, getAccommodationInfo, managerDeleteAccommodation} from "@/net";
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
    document.title = "住宿信息管理" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const changeInfoForm = reactive({
    id: '',
    building: '',
    unit: '',
    room: '',
    bed: ''
})

const addAccommodationForm = reactive({
    id: '',
    building: '',
    unit: '',
    room: '',
    bed: ''
})

const buildingFilterTag = (value: string, row: Accommodation) => {
    return row.building === value
}

const validateId = (rule, value, callback) => {
    if (changeInfoForm.id.length==9 && !/^\d{9}$/.test(value)){
        callback(new Error("学号格式有误"))
    }
    else if (changeInfoForm.id.length==6 && !/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    }
    else if(changeInfoForm.id.length!=6 && changeInfoForm.id.length!=9){
        callback(new Error("学号或工号格式有误"))
    }
    else {
        callback()
    }
}

const rule = {
    id: [
        {required: true, message: '请输入学号 / 工号', trigger: 'blur'},
        {validator: validateId, trigger: ['blur', 'change']}
    ],
    building: [
        {required: true, message: '请输入楼号', trigger: 'blur'},
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'blur'},
    ],
    room: [
        {required: true, message: '请输入房间号', trigger: 'blur'},
    ]
}

const newValidateId = (rule, value, callback) => {
    if (value === '') {
        callback(new Error("请输入学号或工号"))
    } else if (addAccommodationForm.id.length == 9 && !/^\d{9}$/.test(value)) {
        callback(new Error("学号格式有误"))
    } else if (addAccommodationForm.id.length == 6 && !/^\d{2}-\d{3}$/.test(value)) {
        callback(new Error("工号格式有误"))
    } else if (addAccommodationForm.id.length != 6 && addAccommodationForm.id.length != 9) {
        callback(new Error("学号或工号格式有误"))
    } else {
        callback()
    }
}

const newAccommodationRule = {
    id: [
        {required: true, message: '请输入学号 / 工号', trigger: 'blur'},
        {validator: newValidateId, trigger: ['blur', 'change']}
    ],
    building: [
        {required: true, message: '请输入楼号', trigger: 'blur'},
    ],
    unit: [
        {required: true, message: '请选择单元号', trigger: 'blur'},
    ],
    room: [
        {required: true, message: '请输入房间号', trigger: 'blur'},
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


const toggleSelection = (rows?: Accommodation[]) => {
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

const selectionRows = (rows?: Accommodation[]) => {
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

interface Accommodation {
    id: String,
    building: String,
    unit: String,
    room: String,
    bed: String,
}

const form = reactive({
    id: '',
    building: '',
    unit: '',
    room: '',
    bed: '',
})

// 将getAccommodationInfo封装为一个函数，方便调用
function updateInfoTable() {
    loading.value = true;
    getAccommodationInfo(currentPage.value, pageSize.value, {...form}, (data) => {
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

// 删除住宿信息
function deleteAccommodation(row: Accommodation) {
    console.log("deleteAccommodation:" + row.id);
    createDialog("删除住宿信息", "确认删除" + row.id + "的住宿信息吗？").then((res) => {
        if (res){
            managerDeleteAccommodation(row.id, (data) => {
                console.log("data:", data);
                ElMessage.success("住宿信息" + row.id + "删除成功");
                updateInfoTable();
            }, (error) => {
                console.error("删除住宿信息失败", error);
                ElMessage.error("住宿信息" + row.id + "删除失败");
            });
        }
    }, reason => {
        console.log("取消删除住宿信息");
    });
}

// 批量删除住宿信息
function deleteAccommodations(){

}

// 编辑住宿信息浮窗
function editAccommodation(row: Accommodation) {
    console.log("editAccommodation:" + row.id);
    changeInfoForm.id = row.id;
    changeInfoForm.building = row.building;
    changeInfoForm.unit = row.unit;
    changeInfoForm.room = row.room;
    changeInfoForm.bed = row.bed;
    dialogFormVisible.value = true;
}

// 新增账户浮窗
function addNewAccommodation() {
    dialogAddNewFormVisible.value = true;
}

async function isFormValid(){
    const valid = await formRef.value.validate()
    await createDialog("表单检查", "检测结果：" + valid)
    return valid
}

// 等待对话框关闭后，更新表格
function updateAccommodation() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/accommodation/edit', {...changeInfoForm}, (data) => {
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
function createNewAccommodation() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('/api/manage/accommodation/add', {...addAccommodationForm}, (data) => {
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
                        <el-input v-model="form.id" placeholder="学号 / 工号" style="width: 100px; text-align: center"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.building" placeholder="楼栋号" style="width: 100px; text-align: center"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.unit" placeholder="单元号" style="width: 100px; text-align: center"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.room" placeholder="房间号" style="width: 100px; text-align: center"/>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input v-model="form.bed" placeholder="床位号" style="width: 100px; text-align: center"/>
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
                <el-button type="primary" size="small" :icon="CirclePlus" @click="addNewAccommodation">添加住宿信息</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="" disabled>下载模板文件</el-button>
                <el-button type="primary" size="small" :icon="Upload" @click="" disabled>上传批量添加</el-button>
                <el-button type="danger" size="small" :icon="Delete" @click="deleteAccommodations" disabled>批量删除</el-button>
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
            <el-table-column prop="id" label="学号 / 工号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.id)">{{ row.id }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="building"
                             label="楼栋号"
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
                             :filter-method="buildingFilterTag"
                             align="center">
                <template #default="{row}">
                    <el-tag v-if='row.building === "01"'>01栋</el-tag>
                    <el-tag v-if='row.building === "02"'>02栋</el-tag>
                    <el-tag v-if='row.building === "03"'>03栋</el-tag>
                    <el-tag v-if='row.building === "04"'>04栋</el-tag>
                    <el-tag v-if='row.building === "05"'>05栋</el-tag>
                    <el-tag v-if='row.building === "06"'>06栋</el-tag>
                    <el-tag v-if='row.building === "07"'>07栋</el-tag>
                    <el-tag v-if='row.building === "08"'>08栋</el-tag>
                    <el-tag v-if='row.building === "09"'>09栋</el-tag>
                    <el-tag v-if='row.building === "10"'>10栋</el-tag>
                    <el-tag v-if='row.building === "11"'>11栋</el-tag>
                    <el-tag v-if='row.building === "12"'>12栋</el-tag>
                    <el-tag v-if='row.building === "13"'>13栋</el-tag>
                    <el-tag v-if='row.building === "14"'>14栋</el-tag>
                    <el-tag v-if='row.building === "15"'>15栋</el-tag>
                    <el-tag v-if='row.building === "16"'>16栋</el-tag>
                    <el-tag v-if='row.building === "17"'>17栋</el-tag>
                    <el-tag v-if='row.building === "18"'>18栋</el-tag>
                    <el-tag v-if='row.building === "19"'>19栋</el-tag>
                    <el-tag v-if='row.building === "20"'>20栋</el-tag>
                    <el-tag v-if='row.building === "21"'>21栋</el-tag>
                    <el-tag v-if='row.building === "22"'>22栋</el-tag>
                    <el-tag v-if='row.building === "23"'>23栋</el-tag>
                    <el-tag v-if='row.building === "24"'>24栋</el-tag>
                    <el-tag v-if='row.building === "25"'>25栋</el-tag>
                    <el-tag v-if='row.building === "26"'>26栋</el-tag>
                    <el-tag v-if='row.building === "27"'>27栋</el-tag>
                    <el-tag v-if='row.building === "28"'>28栋</el-tag>
                    <el-tag v-if='row.building === "29"'>29栋</el-tag>
                    <el-tag v-if='row.building === "30"'>30栋</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="unit" label="单元号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.unit)">{{ row.unit }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="room" label="房间号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.room)">{{ row.room }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column prop="bed" label="床位号" min-width="140" sortable align="center">
                <template #default="{row}">
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text" @click="copy(row.bed)">{{ row.bed }}</el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
            <!-- 操作列，有两个按钮：编辑和删除，点击编辑按钮跳弹出编辑住宿信息浮窗，点击删除按钮弹出确认删除提示窗，然后删除住宿信息 -->
            <el-table-column prop="id" label="操作" fixed="right" width="200" align="center">
                <template #default="{row}">
                    <el-button type="warning" size="small" :icon="Edit" @click="editAccommodation(row)">编辑</el-button>
                    <el-button type="danger" size="small" :icon="Delete" @click="deleteAccommodation(row)">删除</el-button>
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
    <!-- 浮窗，里面有一个表单，表单中有学号/工号、邮箱、密码、住宿信息状态、用户组、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogFormVisible" title="住宿信息修改">
        <el-form :model="changeInfoForm" :rules="rule" ref="formRef">
            <el-form-item label="学号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="changeInfoForm.id" style="width: 240px" autocomplete="off" disabled/>
            </el-form-item>
            <el-form-item label="楼栋号" :label-width="formLabelWidth" prop="building">
                <el-input v-model="changeInfoForm.building" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="单元号" :label-width="formLabelWidth" prop="unit">
                <el-select v-model="changeInfoForm.unit"
                           style="width: 240px;"
                           placeholder="请选择单元号">
                    <el-option label="A" value="A"/>
                    <el-option label="B" value="B"/>
                </el-select>
            </el-form-item>
            <el-form-item label="房间号" :label-width="formLabelWidth" prop="room">
                <el-input v-model="changeInfoForm.room" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="床位号" :label-width="formLabelWidth" prop="bed">
                <el-input v-model="changeInfoForm.bed" style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateAccommodation" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
    <!-- 新增信息浮窗，里面有一个表单、确认按钮和取消按钮 -->
    <el-dialog v-model="dialogAddNewFormVisible" title="新增住宿信息">
        <el-form :model="addAccommodationForm" :rules="newAccommodationRule" ref="addNewFormRef">
            <el-form-item label="学号" :label-width="formLabelWidth" prop="id">
                <el-input v-model="addAccommodationForm.id" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="楼栋号" :label-width="formLabelWidth" prop="building">
                <el-select v-model="addAccommodationForm.building"
                           style="width: 300px;"
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
            <el-form-item label="单元号" :label-width="formLabelWidth" prop="unit">
                <el-select v-model="addAccommodationForm.unit"
                           style="width: 240px;"
                           placeholder="请选择单元号">
                    <el-option label="A单元" value="A"/>
                    <el-option label="B单元" value="B"/>
                </el-select>
            </el-form-item>
            <el-form-item label="房间号" :label-width="formLabelWidth" prop="room">
                <el-input v-model="addAccommodationForm.room" style="width: 240px" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="床位号" :label-width="formLabelWidth" prop="bed">
                <el-input v-model="addAccommodationForm.bed" style="width: 240px" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
          <el-button @click="isFormValid">检查</el-button>
        <el-button @click="dialogAddNewFormVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewAccommodation" :disabled="!isFormValid">确认</el-button>
      </span>
        </template>
    </el-dialog>
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>