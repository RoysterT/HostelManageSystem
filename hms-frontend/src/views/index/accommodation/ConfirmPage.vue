<script lang="ts" setup>
import {reactive, ref, watch} from "vue";
import {getUserGroup} from "@/views/scripts/UserUtils";
import {get, getStaffDetail, getAccommodationChangeInfoByOrder,
    managerConfirmAccommodationChange, managerRejectAccommodationChange} from "@/net";
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
    List, QuestionFilled, Refresh, Search,
    SuccessFilled,
    Tools
} from "@element-plus/icons-vue";

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const loading = ref(false)
let userId = getUserId();
let infoDrawerVisible = ref(false)
const isToConfirm = ref(true);
const multipleTableRef = ref<InstanceType<typeof ElTable>>()

const pageSizes = ref([1, 2, 5, 10, 20, 30, 50, 100]);
const background = ref(true);
const small = ref(false);

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

const toggleSelection = (rows?: AccommodationChangeInfo[]) => {
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

interface AccommodationChangeInfo {
    recordOrder: string,
    studentIdA: string,
    studentNameA: string,
    buildingA: string,
    unitA: string,
    roomA: string,
    buildingManagerA: string,
    studentIdB: string,
    studentNameB: string,
    buildingB: string,
    unitB: string,
    roomB: string,
    buildingManagerB: string,
    description: string,
    creationTime: string,
    finishTime: string,
    progress: number
}

function updateInfoTable() {
    loading.value = true;
    let type = isToConfirm.value ? "confirm" : "all";
    const url = '/api/dorm/list/' + type
        + '?page=' + currentPage.value
        + '&pageSize=' + pageSize.value;
    get(url, (data) => {
        console.log("获取数据成功", data);
        // 等待随机时间后更新表格
        // const random = Math.floor(Math.random() * 600) + 200;
        const random = 500;
        setTimeout(() => {
            // 将每一组数据的创建时间和完成时间转换为当前时区的时间字符串，如果为空则不转换
            data.data.forEach((item: AccommodationChangeInfo) => {
                if (item.creationTime != null) {
                    item.creationTime = new Date(item.creationTime).toLocaleString();
                }
                if (item.finishTime != null) {
                    item.finishTime = new Date(item.finishTime).toLocaleString();
                }
            })
            tableData.value = data.data;
            total.value = data.total;
            loading.value = false;
        }, random);
    }, (error) => {
        console.error("数据获取失败", error);
        loading.value = false;
    });
}

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
    document.title = "换宿审核" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

const dormChangeInfoDrawerData = reactive({
    recordOrder: "",
    studentIdA: "",
    studentNameA: "",
    studentRoomA: "",
    buildingManagerA: "",
    studentIdB: "",
    studentNameB: "",
    studentRoomB: "",
    buildingManagerB: "",
    description: "",
    creationTime: "",
    finishTime: "",
    progress: 0
})

// 查看换宿详情
function dormChangeInfo(row: AccommodationChangeInfo) {
    getAccommodationChangeInfoByOrder(row.recordOrder, (data) => {
        console.log("data:", data);
        dormChangeInfoDrawerData.recordOrder = data.recordOrder;
        dormChangeInfoDrawerData.studentIdA = data.studentIdA;
        dormChangeInfoDrawerData.studentNameA = data.studentNameA;
        dormChangeInfoDrawerData.studentRoomA = data.buildingA+ "栋" + data.unitA + "单元" + data.roomA + "室";
        dormChangeInfoDrawerData.buildingManagerA = data.buildingManagerA;
        dormChangeInfoDrawerData.studentIdB = data.studentIdB;
        dormChangeInfoDrawerData.studentNameB = data.studentNameB;
        dormChangeInfoDrawerData.studentRoomB = data.buildingB+ "栋" + data.unitB + "单元" + data.roomB + "室";
        dormChangeInfoDrawerData.buildingManagerB = data.buildingManagerB;
        dormChangeInfoDrawerData.description = data.description;
        dormChangeInfoDrawerData.creationTime = data.creationTime;
        dormChangeInfoDrawerData.finishTime = data.finishTime;
        dormChangeInfoDrawerData.progress = data.progress;
        infoDrawerVisible.value = true;
    }, (error) => {
        console.error("获取换宿详情失败", error);
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

// 驳回换宿记录
function rejectAccommodationChange(row: AccommodationChangeInfo) {
    console.log("rejectAccommodationChange:" + row.recordOrder);
    createDialog("驳回换宿申请", "确认驳回换宿申请" + row.recordOrder + "吗？").then((res) => {
        if (res) {
            managerRejectAccommodationChange(row.recordOrder, 7, (data) => {
                console.log("data:", data);
                ElMessage.success("换宿申请" + row.recordOrder + "驳回成功");
                updateInfoTable();
            }, (error) => {
                console.error("驳回换宿申请失败", error);
                ElMessage.error("换宿申请" + row.recordOrder + "驳回失败");
            });
        }
    }, reason => {
        console.log("取消驳回换宿申请");
    })
}

function dormChangeConfirm(row: AccommodationChangeInfo) {
    managerConfirmAccommodationChange(row.recordOrder, (data) => {
        console.log("申请单确认成功", data);
        ElMessage.success("申请单确认成功");
        updateInfoTable();
    }, (error) => {
        console.error("申请单确认失败", error);
        ElMessage.error("申请单确认失败");
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
                <!-- 切换视图按钮，改变isToConfirm的值，即“切换到全部记录”和”切换到待确认记录“ -->
                <el-button type="primary" size="small" :icon="Refresh"
                           @click="isToConfirm = !isToConfirm;updateInfoTable()">
                    <template v-if="isToConfirm">查看全部记录</template>
                    <template v-else>仅查看待确认记录</template>
                </el-button>
            </div>
        </div>
        <div class="table-container">
            <el-table :data="tableData"
                      class-name="table-main"
                      empty-text="当前无待确认信息！"
                      header-align="center"
                      ref="multipleTableRef"
                      v-loading="loading"
                      v-if="mainTableVisable"
                      style="width: 100%; overflow: scroll"
            >
                <el-table-column type="selection" width="80" fixed align="center"/>
                <el-table-column prop="recordOrder" label="记录序号" min-width="140" sortable align="center"/>
                <el-table-column prop="progress" label="进度" min-width="220" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy('等待楼栋管理员A进行确认')"
                                       v-if="row.progress==2">等待楼栋管理员A进行确认</el-button>
                            <el-button type="text" @click="copy('等待楼栋管理员B进行确认')"
                                       v-else-if="row.progress==3">等待楼栋管理员B进行确认</el-button>
                            <el-button type="text" @click="copy('等待学校进行审核')"
                                       v-else-if="row.progress==4">等待校方进行审核</el-button>
                            <el-button type="text" @click="copy('申请完成')"
                                       v-else-if="row.progress==5">申请完成</el-button>
                            <el-button type="text" @click="copy('换宿申请已被楼栋管理员驳回')"
                                       v-else-if="row.progress==6">换宿申请已被楼栋管理员驳回</el-button>
                            <el-button type="text" @click="copy('换宿申请已被学校驳回')"
                                       v-else-if="row.progress==7">换宿申请已被学校驳回</el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="换宿理由" min-width="140" sortable align="center"/>
                <el-table-column prop="creationTime" label="申请时间" min-width="220" sortable align="center">
                    <template #default="{row}">
                        <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                            <el-button type="text" @click="copy(row.creationTime)">{{ row.creationTime }}</el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <!-- 操作列 -->
                <el-table-column prop="dormChangeOrder" label="操作" fixed="right" width="280" align="center"
                                 v-if="isToConfirm">
                    <template #default="{row}">
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="dormChangeInfo(row)">详情</el-button>
                        <el-button type="success" size="small" :icon="SuccessFilled" @click="dormChangeConfirm(row)"
                                   v-if="isToConfirm && row.progress === 4">确认</el-button>
                        <el-button type="success" size="small" :icon="SuccessFilled" v-else disabled>确认</el-button>
                        <el-button type="danger" size="small" :icon="CircleCloseFilled" @click="rejectAccommodationChange(row)"
                                   v-if="isToConfirm && row.progress === 4">驳回</el-button>
                        <el-button type="danger" size="small" :icon="CircleCloseFilled" v-else disabled>驳回</el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="dormChangeOrder" label="操作" fixed="right" width="120" align="center" v-else>
                    <template #default="{row}">
                        <el-button type="primary" size="small" :icon="InfoFilled" @click="dormChangeInfo(row)">
                            详情
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
    <!-- 换宿详情抽屉 -->
    <el-drawer v-model="infoDrawerVisible"
               title="换宿详情"
               :direction="'rtl'"
               :with-header="true"
               size="30%"
    >
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">记录序号[ {{ dormChangeInfoDrawerData.recordOrder }} ]的详情信息</h4>
        </template>
        <!-- 使用描述列表el-descriptions显示内容，字段全部可以可以一键复制 -->
        <el-descriptions :column="1" border>
            <el-descriptions-item label="单号">{{ dormChangeInfoDrawerData.recordOrder }}</el-descriptions-item>
            <el-descriptions-item label="学生A学号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(dormChangeInfoDrawerData.studentIdA)">{{ dormChangeInfoDrawerData.studentIdA }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生A姓名">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(dormChangeInfoDrawerData.studentNameA)">{{ dormChangeInfoDrawerData.studentNameA }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生A宿舍">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button type="text"
                                   @click="copy(dormChangeInfoDrawerData.studentRoomA)">{{ dormChangeInfoDrawerData.studentRoomA }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生A楼栋负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.buildingManagerA)">{{ dormChangeInfoDrawerData.buildingManagerA }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生B学号">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.studentIdB)">{{ dormChangeInfoDrawerData.studentIdB }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生B姓名">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.studentNameB)">{{ dormChangeInfoDrawerData.studentNameB }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生B宿舍">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.studentRoomB)">{{ dormChangeInfoDrawerData.studentRoomB }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="学生B楼栋负责人">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.buildingManagerB)">{{ dormChangeInfoDrawerData.buildingManagerB }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="换宿原因">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.description)">{{ dormChangeInfoDrawerData.description }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.creationTime)">{{ dormChangeInfoDrawerData.creationTime }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
                <template #default>
                    <el-tooltip class="item" effect="dark" content="点击复制" placement="top">
                        <el-button
                            type="text"
                            @click="copy(dormChangeInfoDrawerData.finishTime)">{{ dormChangeInfoDrawerData.finishTime }}</el-button>
                    </el-tooltip>
                </template>
            </el-descriptions-item>
            <el-descriptions-item label="换宿进度">
                <template #default>
                    <el-steps :active="dormChangeInfoDrawerData.progress<5?dormChangeInfoDrawerData.progress-1:5"
                              finish-status="success"
                              align-center
                              direction="vertical"
                              v-if="dormChangeInfoDrawerData.progress<=5"
                    >
                        <el-step title="学生提交换宿申请" :icon="List"></el-step>
                        <el-step title="等待楼栋管理员A进行确认" :icon="Checked"></el-step>
                        <el-step title="等待楼栋管理员B进行确认" :icon="Checked"></el-step>
                        <el-step title="等待学校进行审核" :icon="QuestionFilled"></el-step>
                        <el-step title="申请完成" :icon="SuccessFilled"></el-step>
                    </el-steps>
                    <el-steps :active="2"
                              finish-status="success"
                              align-center
                              process-status="error"
                              direction="vertical"
                              v-else-if="dormChangeInfoDrawerData.progress==6"
                    >
                        <el-step title="学生提交换宿申请" :icon="List"></el-step>
                        <el-step title="等待楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="换宿申请已被楼栋管理员驳回" :icon="CircleCloseFilled"></el-step>
                    </el-steps>
                    <el-steps :active="3"
                              finish-status="success"
                              align-center
                              process-status="error"
                              direction="vertical"
                              v-else-if="dormChangeInfoDrawerData.progress==7"
                    >
                        <el-step title="学生提交换宿申请" :icon="List"></el-step>
                        <el-step title="等待楼栋管理员进行确认" :icon="Checked"></el-step>
                        <el-step title="等待学校进行审核" :icon="QuestionFilled"></el-step>
                        <el-step title="换宿申请已被学校驳回" :icon="CircleCloseFilled"></el-step>
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
</template>

<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>