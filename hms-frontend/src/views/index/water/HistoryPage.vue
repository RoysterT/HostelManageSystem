<script setup>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "订购记录" + " - HMS(" + userGroup + ")";

</script>

<template>
  HistoryPage
</template>

<style scoped>

</style>