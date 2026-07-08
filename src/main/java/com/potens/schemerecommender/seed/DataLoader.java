package com.potens.schemerecommender.seed;

import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.ProfileAttribute;
import com.potens.schemerecommender.enums.RuleType;
import com.potens.schemerecommender.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds government schemes with eligibility rules on application startup.
 * Idempotent: skips if schemes already exist.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final SchemeRepository schemeRepository;

    public DataLoader(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    @Override
    public void run(String... args) {
        if (schemeRepository.count() > 0) {
            log.info("Schemes already seeded — skipping DataLoader");
            return;
        }

        log.info("Seeding government schemes...");

        seedPmKisan();
        seedAyushmanBharat();
        seedPmUjjwala();
        seedPmAwasYojana();
        seedNationalScholarship();
        seedStartupIndia();
        seedMudraLoan();
        seedSukanyaSamriddhi();
        seedPmVishwakarma();
        seedDigitalIndia();
        seedKisanCreditCard();
        seedStandUpIndia();
        seedPmSvanidhi();
        seedNationalPension();
        seedPmFasalBima();
        seedSkillIndia();

        log.info("Seeded {} schemes successfully", schemeRepository.count());
    }

    // ──────────────── Scheme Builders ────────────────

    private void seedPmKisan() {
        Scheme scheme = Scheme.builder()
                .name("PM-KISAN Samman Nidhi")
                .description("Direct income support of Rs 6,000 per year to farmer families")
                .category("Agriculture")
                .ministry("Ministry of Agriculture & Farmers Welfare")
                .benefitAmount("Rs 6,000 per year")
                .benefitDescription("Direct benefit transfer in three equal installments of Rs 2,000")
                .eligibilitySummary("Small and marginal farmer families with cultivable land")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "FARMER", null, null, 30, true, "Applicant must be a farmer"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "180000", null, null, 25, false, "Annual income should be within Rs 1,80,000"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Applicant must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 15, false, "Preference for rural residents"));

        schemeRepository.save(scheme);
    }

    private void seedAyushmanBharat() {
        Scheme scheme = Scheme.builder()
                .name("Ayushman Bharat - PMJAY")
                .description("Health insurance coverage of Rs 5 lakh per family per year for secondary and tertiary hospitalization")
                .category("Healthcare")
                .ministry("Ministry of Health & Family Welfare")
                .benefitAmount("Rs 5,00,000 per year")
                .benefitDescription("Cashless and paperless healthcare at empanelled hospitals")
                .eligibilitySummary("Economically weaker families based on SECC data")
                .build();

        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "500000", null, null, 30, true, "Annual income must be below Rs 5,00,000"));
        scheme.addRule(rule(ProfileAttribute.FAMILY_SIZE, RuleType.MIN_VALUE,
                "1", null, null, 10, true, "Must have at least 1 family member"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "0", null, null, 10, false, "Available for all ages"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 15, false, "Priority for rural households"));

        schemeRepository.save(scheme);
    }

    private void seedPmUjjwala() {
        Scheme scheme = Scheme.builder()
                .name("PM Ujjwala Yojana")
                .description("Free LPG connections to women from below poverty line households")
                .category("Energy")
                .ministry("Ministry of Petroleum & Natural Gas")
                .benefitAmount("Free LPG connection + first refill")
                .benefitDescription("LPG connection with security deposit waiver and first refill free")
                .eligibilitySummary("Adult women from BPL households")
                .build();

        scheme.addRule(rule(ProfileAttribute.GENDER, RuleType.ENUM_MATCH,
                "FEMALE", null, null, 30, true, "Applicant must be female"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "200000", null, null, 25, true, "Household income below Rs 2,00,000 (BPL)"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Applicant must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 15, false, "Priority for rural households"));
        scheme.addRule(rule(ProfileAttribute.CATEGORY, RuleType.ENUM_MATCH,
                "SC", null, null, 10, false, "Priority for SC/ST categories"));

        schemeRepository.save(scheme);
    }

    private void seedPmAwasYojana() {
        Scheme scheme = Scheme.builder()
                .name("PM Awas Yojana - Gramin")
                .description("Financial assistance for construction of pucca houses for rural poor")
                .category("Housing")
                .ministry("Ministry of Rural Development")
                .benefitAmount("Rs 1,20,000 to Rs 1,30,000")
                .benefitDescription("Direct financial assistance for house construction")
                .eligibilitySummary("Houseless families or those living in kutcha/dilapidated houses")
                .build();

        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 30, true, "Must reside in a rural area"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "300000", null, null, 25, true, "Annual income must be below Rs 3,00,000"));
        scheme.addRule(rule(ProfileAttribute.FAMILY_SIZE, RuleType.MIN_VALUE,
                "1", null, null, 10, false, "Families of any size eligible"));
        scheme.addRule(rule(ProfileAttribute.CATEGORY, RuleType.ENUM_MATCH,
                "SC", null, null, 15, false, "Priority for SC/ST categories"));

        schemeRepository.save(scheme);
    }

    private void seedNationalScholarship() {
        Scheme scheme = Scheme.builder()
                .name("National Scholarship for SC/ST Students")
                .description("Post-matric scholarship for SC/ST students pursuing higher education")
                .category("Education")
                .ministry("Ministry of Social Justice & Empowerment")
                .benefitAmount("Varies by course (Rs 5,000 to Rs 20,000 per year)")
                .benefitDescription("Tuition fee reimbursement and maintenance allowance")
                .eligibilitySummary("SC/ST students with family income below Rs 2.5 lakh")
                .build();

        scheme.addRule(rule(ProfileAttribute.CATEGORY, RuleType.ENUM_MATCH,
                "SC", null, null, 30, true, "Must belong to SC or ST category"));
        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "STUDENT", null, null, 25, true, "Must be a student"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "250000", null, null, 20, true, "Family income below Rs 2,50,000"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "16", "35", 10, false, "Preferred age between 16 and 35"));

        schemeRepository.save(scheme);
    }

    private void seedStartupIndia() {
        Scheme scheme = Scheme.builder()
                .name("Startup India Seed Fund")
                .description("Financial assistance for startups for proof of concept and market entry")
                .category("Entrepreneurship")
                .ministry("Department for Promotion of Industry and Internal Trade")
                .benefitAmount("Up to Rs 50,00,000")
                .benefitDescription("Seed funding, mentoring, and incubation support")
                .eligibilitySummary("DPIIT recognized startups incorporated within 2 years")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "SELF_EMPLOYED", null, null, 30, true, "Must be self-employed or entrepreneur"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "18", "50", 15, true, "Age between 18 and 50"));
        scheme.addRule(rule(ProfileAttribute.EDUCATION_LEVEL, RuleType.ENUM_MATCH,
                "GRADUATE", null, null, 20, false, "Preference for graduates"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "2500000", null, null, 15, false, "Early-stage startups with income below Rs 25 lakh"));

        schemeRepository.save(scheme);
    }

    private void seedMudraLoan() {
        Scheme scheme = Scheme.builder()
                .name("PM MUDRA Yojana")
                .description("Micro-credit loans for small and micro enterprises")
                .category("Finance")
                .ministry("Ministry of Finance")
                .benefitAmount("Up to Rs 10,00,000")
                .benefitDescription("Collateral-free loans under Shishu, Kishore, and Tarun categories")
                .eligibilitySummary("Non-corporate, non-farm small and micro enterprises")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "SELF_EMPLOYED", null, null, 30, true, "Must be self-employed"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "1000000", null, null, 20, false, "Priority for lower-income entrepreneurs"));


        schemeRepository.save(scheme);
    }

    private void seedSukanyaSamriddhi() {
        Scheme scheme = Scheme.builder()
                .name("Sukanya Samriddhi Yojana")
                .description("Savings scheme for the girl child with high interest rate")
                .category("Savings")
                .ministry("Ministry of Finance")
                .benefitAmount("Interest rate: 8.2% per annum")
                .benefitDescription("Tax-free returns under Section 80C with maturity at age 21")
                .eligibilitySummary("Parents/guardians of girl child below 10 years of age")
                .build();

        scheme.addRule(rule(ProfileAttribute.GENDER, RuleType.ENUM_MATCH,
                "FEMALE", null, null, 30, true, "Scheme is for girl children"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MAX_VALUE,
                "10", null, null, 25, true, "Girl child must be below 10 years of age"));
        scheme.addRule(rule(ProfileAttribute.FAMILY_SIZE, RuleType.MIN_VALUE,
                "2", null, null, 10, false, "Family with at least 2 members"));

        schemeRepository.save(scheme);
    }

    private void seedPmVishwakarma() {
        Scheme scheme = Scheme.builder()
                .name("PM Vishwakarma Yojana")
                .description("Support for traditional artisans and craftsmen")
                .category("Skill Development")
                .ministry("Ministry of Micro, Small & Medium Enterprises")
                .benefitAmount("Up to Rs 3,00,000 loan + Rs 15,000 toolkit")
                .benefitDescription("Skill training, toolkit incentive, credit support, and market linkage")
                .eligibilitySummary("Traditional artisans working with hands and tools")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "SELF_EMPLOYED", null, null, 30, true, "Must be a self-employed artisan"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "500000", null, null, 20, false, "Priority for lower-income artisans"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 15, false, "Priority for rural artisans"));

        schemeRepository.save(scheme);
    }

    private void seedDigitalIndia() {
        Scheme scheme = Scheme.builder()
                .name("Digital India Internship Scheme")
                .description("Internship opportunities in government digital initiatives")
                .category("Employment")
                .ministry("Ministry of Electronics & IT")
                .benefitAmount("Rs 10,000 per month stipend")
                .benefitDescription("6-month internship with mentoring and certification")
                .eligibilitySummary("Students and recent graduates in IT/CS fields")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "STUDENT", null, null, 30, true, "Must be a student or recent graduate"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "18", "30", 15, true, "Age between 18 and 30"));
        scheme.addRule(rule(ProfileAttribute.EDUCATION_LEVEL, RuleType.ENUM_MATCH,
                "GRADUATE", null, null, 25, false, "Preference for graduates"));

        schemeRepository.save(scheme);
    }

    private void seedKisanCreditCard() {
        Scheme scheme = Scheme.builder()
                .name("Kisan Credit Card")
                .description("Short-term crop loans at subsidized interest rates")
                .category("Agriculture")
                .ministry("Ministry of Agriculture & Farmers Welfare")
                .benefitAmount("Up to Rs 3,00,000 at 4% interest")
                .benefitDescription("Flexible credit for crop production, post-harvest, and allied activities")
                .eligibilitySummary("All farmers including tenant farmers and sharecroppers")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "FARMER", null, null, 30, true, "Must be a farmer"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "18", "75", 10, true, "Age between 18 and 75"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "500000", null, null, 20, false, "Priority for small and marginal farmers"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 15, false, "Preference for rural areas"));

        schemeRepository.save(scheme);
    }

    private void seedStandUpIndia() {
        Scheme scheme = Scheme.builder()
                .name("Stand Up India")
                .description("Bank loans for SC/ST and women entrepreneurs")
                .category("Entrepreneurship")
                .ministry("Ministry of Finance")
                .benefitAmount("Rs 10,00,000 to Rs 1,00,00,000")
                .benefitDescription("Composite loan covering term loan and working capital")
                .eligibilitySummary("SC/ST or women entrepreneurs for greenfield enterprises")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "SELF_EMPLOYED", null, null, 30, true, "Must be self-employed or entrepreneur"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.GENDER, RuleType.ENUM_MATCH,
                "FEMALE", null, null, 20, false, "Priority for women entrepreneurs"));
        scheme.addRule(rule(ProfileAttribute.CATEGORY, RuleType.ENUM_MATCH,
                "SC", null, null, 20, false, "Priority for SC/ST categories"));

        schemeRepository.save(scheme);
    }

    private void seedPmSvanidhi() {
        Scheme scheme = Scheme.builder()
                .name("PM SVANidhi - Street Vendor Scheme")
                .description("Micro-credit facility for street vendors")
                .category("Finance")
                .ministry("Ministry of Housing & Urban Affairs")
                .benefitAmount("Up to Rs 50,000")
                .benefitDescription("Working capital loan with digital payment incentive and interest subsidy")
                .eligibilitySummary("Street vendors in urban areas with valid certificate")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "SELF_EMPLOYED", null, null, 30, true, "Must be self-employed (street vendor)"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "false", null, null, 20, true, "Must be in an urban area"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "300000", null, null, 15, false, "Priority for lower-income vendors"));

        schemeRepository.save(scheme);
    }

    private void seedNationalPension() {
        Scheme scheme = Scheme.builder()
                .name("Atal Pension Yojana")
                .description("Guaranteed pension scheme for unorganized sector workers")
                .category("Pension")
                .ministry("Ministry of Finance")
                .benefitAmount("Rs 1,000 to Rs 5,000 per month pension")
                .benefitDescription("Guaranteed monthly pension after 60 years of age")
                .eligibilitySummary("Indian citizens aged 18-40 with a bank account")
                .build();

        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "18", "40", 30, true, "Age must be between 18 and 40"));
        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "DAILY_WAGE", null, null, 20, false, "Priority for daily wage workers"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "600000", null, null, 15, false, "Priority for lower-income groups"));

        schemeRepository.save(scheme);
    }

    private void seedPmFasalBima() {
        Scheme scheme = Scheme.builder()
                .name("PM Fasal Bima Yojana")
                .description("Crop insurance at minimal premium for farmers")
                .category("Agriculture")
                .ministry("Ministry of Agriculture & Farmers Welfare")
                .benefitAmount("Crop loss coverage up to sum insured")
                .benefitDescription("Insurance against crop loss due to natural calamities, pests, and diseases")
                .eligibilitySummary("All farmers growing notified crops in notified areas")
                .build();

        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "FARMER", null, null, 30, true, "Must be a farmer"));
        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.MIN_VALUE,
                "18", null, null, 10, true, "Must be at least 18 years old"));
        scheme.addRule(rule(ProfileAttribute.IS_RURAL, RuleType.BOOLEAN_CHECK,
                "true", null, null, 20, false, "Priority for rural farmers"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "500000", null, null, 15, false, "Priority for small and marginal farmers"));

        schemeRepository.save(scheme);
    }

    private void seedSkillIndia() {
        Scheme scheme = Scheme.builder()
                .name("Skill India - PMKVY")
                .description("Free skill development training and certification for youth")
                .category("Skill Development")
                .ministry("Ministry of Skill Development & Entrepreneurship")
                .benefitAmount("Free training + Rs 8,000 reward on certification")
                .benefitDescription("Industry-relevant skill training with placement assistance")
                .eligibilitySummary("Indian youth who are school/college dropouts or unemployed")
                .build();

        scheme.addRule(rule(ProfileAttribute.AGE, RuleType.RANGE_CHECK,
                null, "15", "45", 25, true, "Age must be between 15 and 45"));
        scheme.addRule(rule(ProfileAttribute.OCCUPATION, RuleType.ENUM_MATCH,
                "UNEMPLOYED", null, null, 25, false, "Priority for unemployed youth"));
        scheme.addRule(rule(ProfileAttribute.EDUCATION_LEVEL, RuleType.ENUM_MATCH,
                "SECONDARY", null, null, 15, false, "Preference for secondary education holders"));
        scheme.addRule(rule(ProfileAttribute.ANNUAL_INCOME, RuleType.MAX_VALUE,
                "300000", null, null, 10, false, "Priority for lower-income families"));

        schemeRepository.save(scheme);
    }

    // ──────────────── Helper ────────────────

    private SchemeRule rule(ProfileAttribute attribute, RuleType ruleType,
                           String value, String minValue, String maxValue,
                           int weight, boolean mandatory, String description) {
        return SchemeRule.builder()
                .attribute(attribute)
                .ruleType(ruleType)
                .value(value)
                .minValue(minValue)
                .maxValue(maxValue)
                .weight(weight)
                .isMandatory(mandatory)
                .description(description)
                .build();
    }
}
