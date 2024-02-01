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
const headerInfoLoad = ref(false)
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
    document.title = "宿舍卫生评级" + " - HMS(" + userGroup.value + ")";
    mainTableVisable.value = true;
    updateInfoTable();
}, (error) => {
    console.error("获取用户组列表失败", error);
});

getAllActivityArray((data) => {
    console.log("全部活动：", data);
    allActivityArray.value = data;
    headerInfoLoad.value = true;
}, (error) => {
    console.error("获取活动列表失败", error);
});

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

const latestMark = reactive({
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
    loading.value = true;
    let url = '/api/hygiene/score/info/list/' + getUserId()
        + '?page=' + currentPage.value
        + '&pageSize=' + pageSize.value;
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
    url = '/api/hygiene/score/info/latest/' + getUserId();
    get(url, (data) => {
        latestMark.recordId = data.recordId;
        latestMark.activityId = data.activityId;
        latestMark.building = data.building;
        latestMark.unit = data.unit;
        latestMark.room = data.room;
        latestMark.score = data.score;
        latestMark.recordTime = new Date(data.recordTime).toLocaleString();
        latestMark.description = data.description;
        latestMark.progress = data.progress;
    }, (error) => {
        console.error("最新数据获取失败", error);
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

// 查看评分详情
function hygieneInfo(recordId) {
    getHygieneInfo(recordId, (data) => {
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

function rectifyFinish(recordId) {
    console.log("rectifySuccess:" + recordId);
    createDialog("确认完成", "确认完成后不可撤销，是否确认完成？").then((value) => {
        if (value) {
            let url = '/api/hygiene/mark/rectify/finish/' + recordId;
            get(url, (data) => {
                ElMessage.success("确认完成成功！");
                updateInfoTable();
            }, (error) => {
                console.error("确认完成失败", error);
                ElMessage.error("确认完成失败！");
            });
        }
    });
}

</script>

<template>
    <div style="display: flex;flex-direction: column;width: 100%;height: 100%">
        <div style="flex: 1;box-sizing: border-box;margin: 20px;">
            <div style="display: flex;flex-direction: row;height: 100%">
                <!-- 每一块内容区域都设为浮岛样式，即圆角、阴影 -->
                <!-- 左侧评级、序号、分数，flex2份 -->
                <div style="flex: 2;margin: 5px;box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);border-radius: 10px">
                    <div style="display: flex;flex-direction: row;height: 100%">
                        <!-- 评级 -->
                        <div style="flex: 1;text-align: center;height: 100%;display: flex;">
                            <!-- 根据分数显示字母等级，ABCDE五个等级对应90+、80+、70+、60+、60- -->
                            <div style="font-size: 50px;
                                        font-weight: bold;
                                        width: max-content;
                                        height: max-content;
                                        position: relative;
                                        top: 0;
                                        left: 0;
                                        bottom: 0;
                                        right: 0;
                                        margin: auto;
                            ">
                                <template v-if="latestMark.score>=90">A</template>
                                <template v-else-if="latestMark.score>=80">B</template>
                                <template v-else-if="latestMark.score>=70">C</template>
                                <template v-else-if="latestMark.score>=60">D</template>
                                <template v-else>E</template>
                            </div>
                        </div>
                        <!-- 占2行，第一行显示评分序号，第二行显示分数 -->
                        <div style="flex: 1;height: 100%; display: flex;flex-direction: column">
                            <!-- 内容居中 -->
                            <div style="font-size: 20px;font-weight: bold;flex: 1;text-align: center">
                                评分序号：{{ latestMark.recordId }}
                            </div>
                            <div style="font-size: 20px;font-weight: bold;flex: 1;text-align: center">
                                分数：{{ latestMark.score }}
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 中间，活动名称、描述信息，flex3份 -->
                <div style="flex: 3;margin: 5px;box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);border-radius: 10px">
                    <div style="display: flex;flex-direction: column;height: 100%" v-if="headerInfoLoad">
                        <!-- 活动名称 -->
                        <div style="flex: 1;text-align: center;height: 100%;display: flex;">
                            <div style="font-size: 30px;
                                        font-weight: bold;
                                        width: max-content;
                                        height: max-content;
                                        position: relative;
                                        top: 0;
                                        left: 0;
                                        bottom: 0;
                                        right: 0;
                                        margin: auto;
                            ">
                                {{ allActivityArray.find(item => item.value === latestMark.activityId).label }}
                            </div>
                        </div>
                        <el-divider>描述信息</el-divider>
                        <!-- 描述信息 -->
                        <div style="flex: 2;height: 100%; display: flex;flex-direction: column">
                            <!-- 内容居中 -->
                            <div style="font-size: 20px;font-weight: bold;flex: 1;text-align: center">
                                {{ latestMark.description }}
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 右侧，操作按钮，flex1份 -->
                <div style="flex: 1;margin: 5px;box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);border-radius: 10px">
                    <div style="display: flex;flex-direction: column;height: 100%">
                        <!-- 操作按钮 -->
                        <div style="position: relative;
                                    width: max-content;
                                    height: max-content;
                                    top: 0;
                                    left: 0;
                                    bottom: 0;
                                    right: 0;
                                    margin: auto;
                                    display: flex;
                                    flex-direction: row;
                                    justify-content: space-around;
                                    align-items: center;
                        ">
                            <el-button type="primary" size="small" :icon="InfoFilled" style="width: min-content"
                                       @click="hygieneInfo(latestMark.recordId)">详情
                            </el-button>
                            <el-button type="success" size="small" :icon="SuccessFilled" style="width: min-content"
                                       @click="rectifyFinish(latestMark.recordId)" v-if="latestMark.progress==2">确认完成</el-button>
                            <el-button type="success" size="small" :icon="SuccessFilled" style="width: min-content"
                                       @click="rectifyFinish(latestMark.recordId)" v-else disabled>确认完成</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <el-divider>宿舍评分历史记录</el-divider>
        <div class="table-container" style="flex:3">
            <div class="table-header">
                <!-- 操作区域 -->
                <div class="table-header-tools-area">
                    <el-button type="info" size="small" :icon="Refresh" @click="toggleSelection()">清除选择</el-button>
                    <el-button type="warning" size="small" :icon="Refresh" @click="updateInfoTable">刷新数据</el-button>
                </div>
            </div>
            <el-table :data="tableData"
                      class-name="table-main"
                      empty-text="未查询到记录！"
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
                            <el-button type="text"
                                       @click="copy(allActivityArray.find(item => item.value === row.activityId).label)">
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
                <el-table-column prop="repairOrder" label="操作" fixed="right" width="120" align="center">
                    <template #default="{row}">
                        <el-button type="info" size="small" :icon="InfoFilled" @click="hygieneInfo(row.recordId)">详情</el-button>
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
                    <el-button type="text"
                               @click="copy(allActivityArray.find(item => item.value === hygieneInfoDrawerData.activityId).label)">
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
</template>


<style scoped>
@import "./style/table.css";
@import "@/views/style/index.css";
</style>