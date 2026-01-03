package com.example.diaryapp.service

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 离线AI标签分析服务
 * 使用关键词匹配 + ML Kit 进行智能分析
 */
@Singleton
class TagAnalyzerService @Inject constructor(

) {

    // 系统预设标签及其关键词映射
    private val tagKeywords = mapOf(
        "生活" to listOf(
            "日常", "生活", "起床", "睡觉", "休息", "家里", "家", "居家", "放松", "空闲",
            "做饭", "清洁", "打扫", "购物", "逛街", "买菜", "做饭", "吃饭", "晚餐", "午餐",
            "早餐", "周末", "假期", "休息日", "今天", "昨天", "晚上", "早上", "下午"
        ),
        "工作" to listOf(
            "工作", "上班", "公司", "会议", "项目", "任务", "同事", "老板", "客户",
            "报告", "文档", "PPT", "方案", "计划", "目标", "KPI", "加班", "下班", "薪水",
            "工资", "职场", "办公", "出差", "商务", "合作", "谈判", "签约"
        ),
        "学习" to listOf(
            "学习", "读书", "看书", "课程", "教程", "笔记", "复习", "考试", "成绩",
            "学校", "老师", "同学", "作业", "论文", "研究", "知识", "技能", "练习",
            "培训", "讲座", "教育", "学习计划", "英语", "编程", "设计"
        ),
        "旅行" to listOf(
            "旅行", "旅游", "游玩", "景点", "风景", "拍照", "酒店", "机票", "火车",
            "飞机", "行程", "攻略", "出发", "到达", "景点", "海滩", "山", "海",
            "城市", "国家", "出国", "签证", "背包", "自由行", "跟团"
        ),
        "美食" to listOf(
            "美食", "好吃", "美味", "餐厅", "饭店", "小吃", "甜品", "奶茶", "咖啡",
            "菜", "烹饪", "食谱", "味道", "好吃", "难吃", "推荐", "点评", "试吃",
            "零食", "水果", "蛋糕", "面包", "料理"
        ),
        "运动" to listOf(
            "运动", "健身", "跑步", "游泳", "篮球", "足球", "瑜伽", "锻炼", "训练",
            "汗水", "比赛", "健身", "体育场", "体育馆", "步数", "公里", "配速"
        ),
        "心情" to listOf(
            "开心", "快乐", "高兴", "幸福", "难过", "伤心", "生气", "愤怒", "焦虑",
            "担心", "害怕", "紧张", "放松", "平静", "感动", "失望", "兴奋", "期待",
            "沮丧", "郁闷", "愉快", "痛苦", "喜悦", "悲伤", "愤怒", "恐惧"
        ),
        "感悟" to listOf(
            "感悟", "思考", "想法", "观点", "理念", "哲学", "人生", "价值观", "意义",
            "成长", "进步", "改变", "提升", "突破", "启发", "领悟", "体会", "经验",
            "教训", "反思", "总结", "智慧", "真理"
        ),
        "家庭" to listOf(
            "家庭", "家人", "父母", "爸爸", "妈妈", "孩子", "儿子", "女儿", "兄弟",
            "姐妹", "爷爷奶奶", "外公外婆", "配偶", "丈夫", "妻子", "亲情", "团聚"
        ),
        "朋友" to listOf(
            "朋友", "友谊", "聚会", "约会", "聊天", "聚餐", "活动", "伙伴", "好友",
            "闺蜜", "兄弟", "同学", "聚会", "派对", "KTV", "酒吧", "烧烤"
        ),
        "健康" to listOf(
            "健康", "身体", "生病", "感冒", "发烧", "医院", "医生", "药", "治疗",
            "康复", "体检", "锻炼", "睡眠", "疲劳", "精力", "养生", "保健"
        ),
        "娱乐" to listOf(
            "电影", "电视剧", "综艺", "动漫", "游戏", "音乐", "演唱会", "展览",
            "博物馆", "看剧", "追剧", "追星", "偶像", "明星", "娱乐", "休闲"
        ),
        "购物" to listOf(
            "购物", "买东西", "淘宝", "京东", "快递", "包裹", "下单", "付款", "优惠",
            "打折", "促销", "商场", "超市", "买", "购买", "消费"
        ),
        "节日" to listOf(
            "春节", "中秋", "国庆", "端午", "清明", "元旦", "圣诞", "情人节", "七夕",
            "生日", "纪念日", "节日", "庆祝", "祝福", "礼物", "假期", "放假"
        ),
        "梦想" to listOf(
            "梦想", "目标", "理想", "追求", "愿望", "期待", "希望", "憧憬", "未来",
            "规划", "计划", "梦想成真", "实现", "达成"
        )
    )

    // 时间相关标签
    private val timeKeywords = mapOf(
        "早晨" to listOf("早上", "清晨", "早晨", "黎明", "日出", "醒来", "起床"),
        "中午" to listOf("中午", "午餐", "午休", "正午"),
        "傍晚" to listOf("傍晚", "黄昏", "夕阳", "日落"),
        "深夜" to listOf("晚上", "深夜", "夜", "夜班", "熬夜", "睡前", "午夜"),
        "春天" to listOf("春", "春天", "春季", "三月", "四月", "五月"),
        "夏天" to listOf("夏", "夏天", "夏季", "六月", "七月", "八月", "炎热"),
        "秋天" to listOf("秋", "秋天", "秋季", "九月", "十月", "十一月", "落叶"),
        "冬天" to listOf("冬", "冬天", "冬季", "十二月", "一月", "二月", "寒冷", "雪")
    )

    /**
     * 分析文本内容，返回自动生成的标签
     */
    fun analyzeTags(title: String, content: String): List<String> {
        val combinedText = "$title $content"
        val detectedTags = mutableSetOf<String>()

        // 1. 基于关键词匹配
        tagKeywords.forEach { (tag, keywords) ->
            if (keywords.any { combinedText.contains(it, ignoreCase = true) }) {
                detectedTags.add(tag)
            }
        }

        // 2. 检测时间相关标签
        timeKeywords.forEach { (tag, keywords) ->
            if (keywords.any { combinedText.contains(it, ignoreCase = true) }) {
                detectedTags.add(tag)
            }
        }

        // 3. 基于文本长度判断
        if (combinedText.length > 500) {
            detectedTags.add("长文")
        }

        // 4. 检测是否有问号，可能是思考类
        if (combinedText.contains("？") || combinedText.contains("?")) {
            detectedTags.add("思考")
        }

        // 5. 检测感叹号数量，判断情绪强度
        val exclamationCount = combinedText.count { it == '！' || it == '!' }
        if (exclamationCount >= 3) {
            detectedTags.add("激动")
        }

        // 6. 检测数字，可能是记录类
        if (combinedText.contains(Regex("\\d+"))) {
            detectedTags.add("记录")
        }

        // 7. 基于第一人称判断
        if (combinedText.contains("我")) {
            detectedTags.add("个人")
        }

        return detectedTags.toList().sortedBy { it }
    }

    /**
     * 从图片路径中提取可能的标签（基于文件名等）
     */
    fun extractTagsFromImage(imagePath: String): List<String> {
        val tags = mutableSetOf<String>()
        val fileName = imagePath.substringAfterLast("/").toLowerCase()

        // 基于文件名关键词
        tagKeywords.forEach { (tag, keywords) ->
            keywords.forEach { keyword ->
                if (fileName.contains(keyword, ignoreCase = true)) {
                    tags.add(tag)
                }
            }
        }

        return tags.toList()
    }

    /**
     * 合并自动标签和手动标签
     */
    fun mergeTags(autoTags: List<String>, manualTags: List<String>): List<String> {
        return (autoTags + manualTags).distinct().sorted()
    }

    /**
     * 获取所有系统支持的标签
     */
    fun getAllSupportedTags(): List<String> {
        return (tagKeywords.keys + timeKeywords.keys).sorted()
    }

    /**
     * 检查标签是否为系统预设标签
     */
    fun isSystemTag(tag: String): Boolean {
        return tagKeywords.containsKey(tag) || timeKeywords.containsKey(tag)
    }

    /**
     * 根据已选标签推荐相关标签
     */
    fun getRelatedTags(selectedTags: List<String>): List<String> {
        val related = mutableSetOf<String>()

        // 如果选择了"工作"，推荐"学习"、"会议"
        if (selectedTags.contains("工作")) {
            related.addAll(listOf("会议", "项目", "加班"))
        }

        // 如果选择了"旅行"，推荐"美食"、"拍照"
        if (selectedTags.contains("旅行")) {
            related.addAll(listOf("美食", "拍照", "风景"))
        }

        // 如果选择了"美食"，推荐"餐厅"、"做饭"
        if (selectedTags.contains("美食")) {
            related.addAll(listOf("餐厅", "做饭", "购物"))
        }

        // 如果选择了时间标签，推荐相应的心情标签
        if (selectedTags.any { it in listOf("早晨", "早上") }) {
            related.add("活力")
        }
        if (selectedTags.any { it in listOf("深夜", "晚上") }) {
            related.addAll(listOf("思考", "感悟"))
        }

        return related.filter { !selectedTags.contains(it) }.sorted()
    }

    /**
     * 计算两个标签之间的关联度
     */
    fun calculateTagRelevance(tag1: String, tag2: String): Float {
        // 如果是同一个标签
        if (tag1 == tag2) return 1.0f

        // 如果都在同一类别（主题或时间）
        val sameCategory = (tagKeywords.containsKey(tag1) && tagKeywords.containsKey(tag2)) ||
                (timeKeywords.containsKey(tag1) && timeKeywords.containsKey(tag2))

        if (sameCategory) return 0.3f

        // 特定组合有更高关联度
        val highRelevancePairs = listOf(
            Pair("旅行", "美食"),
            Pair("旅行", "拍照"),
            Pair("工作", "学习"),
            Pair("家庭", "朋友"),
            Pair("心情", "感悟"),
            Pair("运动", "健康"),
            Pair("晚上", "思考"),
            Pair("早上", "活力")
        )

        highRelevancePairs.forEach { pair ->
            if ((pair.first == tag1 && pair.second == tag2) ||
                (pair.first == tag2 && pair.second == tag1)) {
                return 0.7f
            }
        }

        return 0.1f // 默认低关联度
    }
}
