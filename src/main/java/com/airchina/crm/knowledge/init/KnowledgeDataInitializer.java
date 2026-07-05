package com.airchina.crm.knowledge.init;

import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.repository.KnowledgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 知识库数据初始化
 *
 * 启动时自动加载示例数据到 ES
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeDataInitializer implements CommandLineRunner {

    private final KnowledgeRepository knowledgeRepository;

    @Override
    public void run(String... args) {
        // 检查是否已有数据
        if (knowledgeRepository.count() > 0) {
            log.info("知识库已有数据，跳过初始化");
            return;
        }

        log.info("开始初始化知识库数据...");

        List<KnowledgeArticle> articles = Arrays.asList(
            // ========== 平台信息 ==========
            KnowledgeArticle.builder()
                    .title("凤凰知音APP下载")
                    .content("凤凰知音APP可通过以下方式下载：\n1. iOS用户：App Store搜索\"凤凰知音\"\n2. Android用户：应用宝、华为应用市场、小米应用商店搜索\"凤凰知音\"\n3. 官网下载：www.airchina.com → 服务 → 移动应用\n\nAPP功能：里程查询、在线值机、航班动态、电子登机牌、权益兑换等。")
                    .category("PLATFORM")
                    .tags(Arrays.asList("APP", "下载", "移动端"))
                    .keywords("凤凰知音 APP 下载 手机 移动应用")
                    .priority(10)
                    .viewCount(1500)
                    .build(),

            KnowledgeArticle.builder()
                    .title("国航官网及联系方式")
                    .content("国航官方网站：www.airchina.com\n客服热线：95583（24小时）\n凤凰知音会员热线：4008-100-999\n海外客服：+86-10-95583\n\n营业时间：\n- 客服热线：24小时\n- 会员热线：08:00-22:00\n- 营业部：周一至周五 09:00-17:30")
                    .category("PLATFORM")
                    .tags(Arrays.asList("官网", "电话", "客服"))
                    .keywords("官网 电话 客服 联系方式 95583")
                    .priority(10)
                    .viewCount(2000)
                    .build(),

            KnowledgeArticle.builder()
                    .title("全国主要营业部地址")
                    .content("北京营业部：北京市朝阳区霄云路36号国航大厦\n上海营业部：上海市浦东新区启航路168号\n广州营业部：广州市天河区林和西路1号\n深圳营业部：深圳市福田区深南大道6008号\n成都营业部：成都市青羊区人民中路一段15号\n\n营业时间：周一至周五 09:00-17:30\n服务内容：客票销售、退改签、会员服务、投诉处理")
                    .category("PLATFORM")
                    .tags(Arrays.asList("营业部", "地址", "网点"))
                    .keywords("营业部 地址 网点 北京 上海 广州")
                    .priority(8)
                    .viewCount(800)
                    .build(),

            // ========== 票规政策 ==========
            KnowledgeArticle.builder()
                    .title("退改签收费标准（2024版）")
                    .content("一、自愿退票\n1. 头等舱/F舱：起飞前免费，起飞后收取10%\n2. 公务舱/C舱：起飞前收取5%，起飞后收取20%\n3. 经济舱/Y舱：起飞前收取10%，起飞后收取30%\n4. 经济舱折扣票：起飞前收取20%，起飞后收取50%\n\n二、自愿改签\n1. 头等舱：免费改签\n2. 公务舱：收取5%改签费\n3. 经济舱：收取10%改签费\n\n三、非自愿退改\n航班取消、延误：免费退改签\n病退：提供医院证明，免费退票")
                    .category("TICKET")
                    .tags(Arrays.asList("退票", "改签", "收费"))
                    .keywords("退票 改签 收费 标准 费用")
                    .priority(10)
                    .viewCount(3000)
                    .build(),

            KnowledgeArticle.builder()
                    .title("行李托运规定")
                    .content("一、国内航班\n- 头等舱：免费托运40公斤\n- 公务舱：免费托运30公斤\n- 经济舱：免费托运20公斤\n- 超重费用：每公斤按经济舱全价的1.5%计算\n\n二、国际航班\n- 头等舱：2件，每件32公斤\n- 公务舱：2件，每件32公斤\n- 经济舱：2件，每件23公斤\n\n三、特殊行李\n- 轮椅：免费托运\n- 婴儿车：免费托运\n- 乐器：可购买占座行李票")
                    .category("TICKET")
                    .tags(Arrays.asList("行李", "托运", "重量"))
                    .keywords("行李 托运 重量 超重 规定")
                    .priority(9)
                    .viewCount(2500)
                    .build(),

            // ========== 常见问题 ==========
            KnowledgeArticle.builder()
                    .title("如何查询里程余额")
                    .content("查询里程余额有以下方式：\n\n1. APP查询（推荐）\n   打开凤凰知音APP → 我的账户 → 里程余额\n\n2. 官网查询\n   登录www.airchina.com → 凤凰知音 → 我的账户\n\n3. 电话查询\n   拨打4008-100-999 → 按2查询里程\n\n4. 营业部查询\n   携带身份证到营业部柜台查询\n\n注意：里程有效期为36个月，过期自动清零")
                    .category("FAQ")
                    .tags(Arrays.asList("里程", "查询", "余额"))
                    .keywords("里程 查询 余额 积分 有效期")
                    .priority(10)
                    .viewCount(5000)
                    .build(),

            KnowledgeArticle.builder()
                    .title("密码重置流程")
                    .content("会员密码重置方式：\n\n方式一：APP自助重置（推荐）\n1. 打开凤凰知音APP\n2. 点击\"忘记密码\"\n3. 输入会员号和手机号\n4. 获取验证码\n5. 设置新密码\n\n方式二：客服重置\n1. 拨打4008-100-999\n2. 提供会员号和身份证号\n3. 客服验证身份后重置\n4. 短信发送临时密码\n\n方式三：营业部重置\n1. 携带身份证原件\n2. 到营业部柜台办理\n3. 当场重置密码")
                    .category("FAQ")
                    .tags(Arrays.asList("密码", "重置", "忘记"))
                    .keywords("密码 重置 忘记 找回 登录")
                    .priority(9)
                    .viewCount(3000)
                    .build(),

            // ========== 服务流程 ==========
            KnowledgeArticle.builder()
                    .title("轮椅服务申请流程")
                    .content("一、申请条件\n- 行动不便的旅客\n- 年长旅客（70岁以上建议）\n- 伤病旅客\n\n二、申请方式\n1. 购票时申请：在购票备注中说明\n2. APP申请：订单详情 → 特殊服务 → 轮椅\n3. 客服申请：拨打95583\n4. 机场申请：至少提前2小时到机场柜台\n\n三、服务内容\n- 值机→登机→下机全程轮椅服务\n- 优先登机\n- 专人陪同\n\n四、注意事项\n- 国内航班：建议提前24小时申请\n- 国际航班：建议提前48小时申请\n- 每个航班轮椅数量有限，建议尽早申请")
                    .category("SERVICE")
                    .tags(Arrays.asList("轮椅", "特殊服务", "申请"))
                    .keywords("轮椅 服务 申请 特殊 行动不便")
                    .priority(10)
                    .viewCount(1500)
                    .build(),

            KnowledgeArticle.builder()
                    .title("贵宾休息室使用规则")
                    .content("一、使用条件\n1. 白金卡会员：本人免费使用\n2. 金卡会员：本人免费使用\n3. 银卡会员：需使用里程兑换\n4. 头等舱/公务舱旅客：免费使用\n\n二、可携带人数\n- 白金卡：可携带1人\n- 金卡：仅限本人\n- 头等舱：可携带1人\n- 公务舱：仅限本人\n\n三、服务内容\n- 免费餐饮\n- 免费WiFi\n- 航班信息提醒\n- 舒适休息区\n- 淋浴室（部分休息室）\n\n四、国内主要休息室\n- 北京T3：国际出发区3层\n- 上海T2：国内出发区2层\n- 广州T2：国内出发区3层")
                    .category("SERVICE")
                    .tags(Arrays.asList("休息室", "贵宾", "VIP"))
                    .keywords("休息室 贵宾 VIP 使用 规则")
                    .priority(9)
                    .viewCount(2000)
                    .build(),

            // ========== 权益说明 ==========
            KnowledgeArticle.builder()
                    .title("白金卡权益一览")
                    .content("一、里程权益\n- 里程累积系数：150%\n- 里程有效期：36个月\n- 兑换优先权\n\n二、出行权益\n- 贵宾休息室：免费使用，可携1人\n- 优先值机：专属柜台\n- 优先登机\n- 行李优先\n- 免费升舱（视情况）\n\n三、服务权益\n- 专属客服热线\n- 优先接听\n- 优先处理投诉\n- 生日里程赠送\n\n四、合作伙伴\n- 酒店集团：万豪、希尔顿、洲际\n- 租车公司：赫兹、安飞士\n- 信用卡：中信国航联名卡")
                    .category("BENEFITS")
                    .tags(Arrays.asList("白金卡", "权益", "等级"))
                    .keywords("白金卡 权益 等级 VIP 特权")
                    .priority(10)
                    .viewCount(4000)
                    .build(),

            KnowledgeArticle.builder()
                    .title("里程兑换机票规则")
                    .content("一、兑换标准\n- 国内经济舱：12000里程起\n- 国内公务舱：20000里程起\n- 国内头等舱：30000里程起\n- 国际经济舱：30000里程起\n\n二、兑换规则\n1. 需提前3天兑换\n2. 每个航班有里程兑换座位限制\n3. 不可兑换的日期：春节、国庆等高峰期\n4. 兑换后不可改签，可退票（扣50%里程）\n\n三、兑换方式\n1. APP兑换：首页 → 里程兑换 → 机票\n2. 官网兑换：登录后 → 里程兑换\n3. 客服兑换：拨打4008-100-999\n\n四、注意事项\n- 兑换人必须是会员本人\n- 需支付机场建设费和燃油附加费")
                    .category("BENEFITS")
                    .tags(Arrays.asList("里程", "兑换", "机票"))
                    .keywords("里程 兑换 机票 规则 标准")
                    .priority(9)
                    .viewCount(3500)
                    .build(),

            // ========== 话术模板 ==========
            KnowledgeArticle.builder()
                    .title("开场白标准话术")
                    .content("一、标准开场白\n\"您好，欢迎致电国航凤凰知音会员服务中心，工号XXX，很高兴为您服务，请问有什么可以帮您？\"\n\n二、回访开场白\n\"您好，请问是XXX先生/女士吗？我是国航凤凰知音会员服务中心的XXX，之前您反馈的XXX问题，想跟您做个回访，方便吗？\"\n\n三、投诉处理开场白\n\"您好，非常抱歉给您带来不便，我是XXX，会全程跟进处理您的问题，请您详细描述一下情况...\"\n\n四、注意事项\n- 语速适中，吐字清晰\n- 保持微笑（声音会传递情绪）\n- 主动倾听，不打断客户\n- 适时回应：\"是的\"\"我理解\"")
                    .category("SCRIPT")
                    .tags(Arrays.asList("话术", "开场白", "标准"))
                    .keywords("话术 开场白 标准 接听 您好")
                    .priority(10)
                    .viewCount(2500)
                    .build(),

            KnowledgeArticle.builder()
                    .title("投诉处理标准话术")
                    .content("一、安抚情绪\n\"非常抱歉给您带来不好的体验，我完全理解您的心情，我会尽快帮您处理...\"\n\n二、了解情况\n\"请您详细描述一下当时的情况，包括时间、地点、航班号等信息，这样我能更准确地帮您处理...\"\n\n三、提出方案\n\"根据您的情况，我们可以提供以下解决方案：1. XXX 2. XXX，您看哪个方案更合适？\"\n\n四、跟进承诺\n\"我已经记录了您的问题，会在XX小时内给您回复，期间您可以随时拨打95583查询进度...\"\n\n五、结束语\n\"感谢您的理解和支持，如果还有其他问题，随时联系我们，祝您旅途愉快！\"\n\n禁忌用语：\n- \"这不是我们的问题\"\n- \"我不知道\"\n- \"你找别人吧\"")
                    .category("SCRIPT")
                    .tags(Arrays.asList("话术", "投诉", "处理"))
                    .keywords("投诉 处理 话术 安抚 解决")
                    .priority(10)
                    .viewCount(3000)
                    .build(),

            // ========== 系统操作 ==========
            KnowledgeArticle.builder()
                    .title("CRM系统工单处理流程")
                    .content("一、接收工单\n1. 登录CRM系统\n2. 查看待办工单列表\n3. 点击工单查看详情\n\n二、处理工单\n1. 确认服务类型和需求\n2. 联系相关部门或人员\n3. 记录处理进度\n4. 更新工单状态\n\n三、工单状态流转\n- 新建 → 已指派 → 处理中 → 已完成 → 已关闭\n- 每次状态变更需填写备注\n\n四、SLA要求\n- 紧急工单：2小时内响应\n- 普通工单：24小时内响应\n- 低优先级：48小时内响应\n\n五、注意事项\n- 工单超时会自动告警\n- 无法处理的工单需及时升级\n- 处理完成后需客户确认")
                    .category("SYSTEM")
                    .tags(Arrays.asList("工单", "CRM", "操作"))
                    .keywords("工单 CRM 操作 流程 系统")
                    .priority(8)
                    .viewCount(1000)
                    .build()
        );

        // 设置公共字段
        LocalDateTime now = LocalDateTime.now();
        articles.forEach(article -> {
            article.setStatus(1);
            article.setCreateTime(now);
            article.setUpdateTime(now);
            article.setCreateBy("system");
            if (article.getViewCount() == null) {
                article.setViewCount(0);
            }
        });

        knowledgeRepository.saveAll(articles);
        log.info("知识库初始化完成，共 {} 条数据", articles.size());
    }
}
