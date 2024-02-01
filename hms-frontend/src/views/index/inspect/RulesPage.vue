<script setup>
import {ref} from "vue";
import {getUserIdentity} from "@/views/scripts/UserUtils";
import {getIdentityList} from "@/net";
let userGroup = ref("");
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
    document.title = "卫生规范条例" + " - HMS(" + userGroup.value + ")";
}, (error) => {
    console.error("获取用户组列表失败", error);
});
</script>

<template>

    <!--    宿舍卫生规范条例

        一、制定目的

        为了维护宿舍卫生，创造一个整洁、舒适的居住环境，特制定本卫生规范条例。

        三、清扫频率与时间

        每天早晨，值日生需在8:00前完成个人寝室的扫地工作。
        每周一上午9:00进行公共区域的全面清扫，包括走廊、卫生间、洗漱间等。
        每月进行一次大扫除，由宿舍全体成员参与，彻底清洁宿舍各个角落。

        四、清洁标准

        地面：保持地面无垃圾、无积水、无灰尘，做到随时清洁。
        桌面：桌面物品摆放整齐，无杂物，无灰尘。
        床铺：床铺整洁，被子叠好，床单干净，无杂物。
        门窗：门窗洁净，无灰尘，无污渍。
        卫生间：保持卫生间清洁，无异味，洗漱用品摆放整齐。
        洗漱间：保持洗漱间整洁，水池无污渍，地面无积水。

        五、处罚措施

        对于未按照清扫频率与时间完成清洁工作的宿舍，将给予警告。
        对于连续多次未完成清洁工作的宿舍，将通知宿舍管理员进行额外清洁，并处以罚款。
        对于严重违反清洁标准的宿舍，将通知宿舍管理员进行整改，并处以罚款。
        对于拒不整改的宿舍，将上报学校相关部门进行处理。

        六、监督机制

        宿舍管理员将定期对宿舍进行巡查，确保卫生规范条例得到执行。
        学生干部可参与宿舍卫生监督工作，对违反卫生规范条例的行为进行记录并上报。
        学生宿舍管理委员会将定期对宿舍卫生情况进行抽查，并将检查结果公布在宿舍公告栏上。-->

    <!-- 给页面添加一些浅色的底色 -->
    <div class="page">
        <!-- 正文部分的容器框，圆角，有阴影 -->
        <div class="rule-container">
            <!-- 标题 -->
            <div class="title">宿舍卫生规范条例</div>
            <!-- 正文 -->
            <div class="content">
                <div class="content-item">
                    <div class="content-item-title">一、制定目的</div>
                    <div class="content-item-content">
                        为了维护宿舍卫生，创造一个整洁、舒适的居住环境，特制定本卫生规范条例。
                    </div>
                </div>
                <div class="content-item">
                    <div class="content-item-title">二、清扫频率与时间</div>
                    <div class="content-item-content">
                        <div class="content-item-content-item">
                            每天早晨，值日生需在8:00前完成个人寝室的扫地工作。
                        </div>
                        <div class="content-item-content-item">
                            每周一上午9:00进行公共区域的全面清扫，包括走廊、卫生间、洗漱间等。
                        </div>
                        <div class="content-item-content-item">
                            每月进行一次大扫除，由宿舍全体成员参与，彻底清洁宿舍各个角落。
                        </div>
                    </div>
                </div>
                <div class="content-item">
                    <div class="content-item-title">三、清洁标准</div>
                    <div class="content-item-content">
                        <div class="content-item-content-item">
                            地面：保持地面无垃圾、无积水、无灰尘，做到随时清洁。
                        </div>
                        <div class="content-item-content-item">
                            桌面：桌面物品摆放整齐，无杂物，无灰尘。
                        </div>
                        <div class="content-item-content-item">
                            床铺：床铺整洁，被子叠好，床单干净，无杂物。
                        </div>
                        <div class="content-item-content-item">
                            门窗：门窗洁净，无灰尘，无污渍。
                        </div>
                        <div class="content-item-content-item">
                            卫生间：保持卫生间清洁，无异味，洗漱用品摆放整齐。
                        </div>
                        <div class="content-item-content-item">
                            洗漱间：保持洗漱间整洁，水池无污渍，地面无积水。
                        </div>
                    </div>
                </div>
                <div class="content-item">
                    <div class="content-item-title">四、处罚措施</div>
                    <div class="content-item-content">
                        <div class="content-item-content-item">
                            对于未按照清扫频率与时间完成清洁工作的宿舍，将给予警告。
                        </div>
                        <div class="content-item-content-item">
                            对于连续多次未完成清洁工作的宿舍，将通知宿舍管理员进行额外清洁，并处以罚款。
                        </div>
                        <div class="content-item-content-item">
                            对于严重违反清洁标准的宿舍，将通知宿舍管理员进行整改，并处以罚款。
                        </div>
                        <div class="content-item-content-item">
                            对于拒不整改的宿舍，将上报学校相关部门进行处理。
                        </div>
                    </div>
                </div>
                <div class="content-item">
                    <div class="content-item-title">五、监督机制</div>
                    <div class="content-item-content">
                        <div class="content-item-content-item">
                            宿舍管理员将定期对宿舍进行巡查，确保卫生规范条例得到执行。
                        </div>
                        <div class="content-item-content-item">
                            学生干部可参与宿舍卫生监督工作，对违反卫生规范条例的行为进行记录并上报。
                        </div>
                        <div class="content-item-content-item">
                            学生宿舍管理委员会将定期对宿舍卫生情况进行抽查，并将检查结果公布在宿舍公告栏上。
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
@import "./style/table.css";
@import "@/views/style/index.css";

.page {
    background-color: #F5F5F5;
    padding: 20px;
}

.rule-container {
    width: 680px;
    height: 100%;
    background-color: #FFFFFF;
    border-radius: 10px;
    box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.15);
    padding: 20px;
    margin: 40px auto;
}

.title {
    font-size: 24px;
    font-weight: bold;
    color: #333333;
    margin-bottom: 20px;
}

.content {
    font-size: 16px;
    color: #666666;
}

.content-item {
    margin-bottom: 20px;
}

.content-item-title {
    font-size: 18px;
    font-weight: bold;
    color: #333333;
    margin-bottom: 10px;
}

.content-item-content {
    margin-left: 20px;
}

.content-item-content-item {
    margin-bottom: 10px;
}
</style>