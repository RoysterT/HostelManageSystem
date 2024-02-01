<script setup>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "饮用水订购管理" + " - HMS(" + userGroup + ")";

</script>

<template>
  ManagePage
</template>

<style scoped>

</style>