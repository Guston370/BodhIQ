package com.mit.bodhiq.data.provider;

import com.mit.bodhiq.data.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class providing comprehensive mock pharmaceutical data for 5 molecules:
 * Montelukast, Humira, Metformin, GLP-1 Agonists, and Eliquis
 */
public class MockDataProvider {
    
    private static MockDataProvider instance;
    private final Map<String, MarketData> marketDataMap;
    private final Map<String, List<PatentInfo>> patentDataMap;
    private final Map<String, List<ClinicalTrial>> clinicalTrialDataMap;
    private final Map<String, List<EximTrade>> eximTradeDataMap;
    private final Map<String, List<Publication>> publicationDataMap;
    
    // Supported molecules
    public static final String MONTELUKAST = "Montelukast";
    public static final String HUMIRA = "Humira";
    public static final String METFORMIN = "Metformin";
    public static final String GLP1 = "GLP-1";
    public static final String ELIQUIS = "Eliquis";
    
    private MockDataProvider() {
        marketDataMap = new HashMap<>();
        patentDataMap = new HashMap<>();
        clinicalTrialDataMap = new HashMap<>();
        eximTradeDataMap = new HashMap<>();
        publicationDataMap = new HashMap<>();
        
        initializeAllData();
    }
    
    public static synchronized MockDataProvider getInstance() {
        if (instance == null) {
            instance = new MockDataProvider();
        }
        return instance;
    }
    
    private void initializeAllData() {
        initializeMarketData();
        initializePatentData();
        initializeClinicalTrialData();
        initializeEximTradeData();
        initializePublicationData();
    }
    
    // Public methods to retrieve data by molecule
    public MarketData getMarketData(String molecule) {
        return marketDataMap.get(molecule);
    }
    
    public List<PatentInfo> getPatentData(String molecule) {
        return patentDataMap.getOrDefault(molecule, new ArrayList<>());
    }
    
    public List<ClinicalTrial> getClinicalTrialData(String molecule) {
        return clinicalTrialDataMap.getOrDefault(molecule, new ArrayList<>());
    }
    
    public List<EximTrade> getEximTradeData(String molecule) {
        return eximTradeDataMap.getOrDefault(molecule, new ArrayList<>());
    }
    
    public List<Publication> getPublicationData(String molecule) {
        return publicationDataMap.getOrDefault(molecule, new ArrayList<>());
    }
    
    public List<String> getSupportedMolecules() {
        return Arrays.asList(MONTELUKAST, HUMIRA, METFORMIN, GLP1, ELIQUIS);
    }
    
    private void initializeMarketData() {
        // Montelukast Market Data
        List<Competitor> montelukastCompetitors = Arrays.asList(
            new Competitor("Merck & Co.", "Singulair", 35.2, "Global", "1998", "Asthma", "First-mover advantage"),
            new Competitor("Teva Pharmaceuticals", "Generic Montelukast", 28.5, "Global", "2012", "Asthma", "Cost advantage"),
            new Competitor("Mylan", "Montelukast Sodium", 15.3, "US/EU", "2013", "Asthma", "Market penetration"),
            new Competitor("Sandoz", "Montelukast Generic", 12.1, "EU", "2014", "Asthma", "Regulatory expertise")
        );
        
        MarketData montelukastMarket = new MarketData(
            MONTELUKAST, 2850, 4.2, "Global",
            Arrays.asList("Asthma", "Allergic Rhinitis", "Exercise-induced Bronchoconstriction"),
            Arrays.asList("Chronic Urticaria", "Atopic Dermatitis"),
            montelukastCompetitors,
            Arrays.asList("Rising asthma prevalence", "Increasing air pollution", "Growing awareness"),
            Arrays.asList("Generic competition", "Alternative therapies", "Regulatory changes"),
            "Steady growth expected with emerging market expansion",
            3650.0
        );
        marketDataMap.put(MONTELUKAST, montelukastMarket);
        
        // Humira Market Data
        List<Competitor> humiraCompetitors = Arrays.asList(
            new Competitor("AbbVie", "Humira", 45.8, "Global", "2002", "Rheumatoid Arthritis", "Market leader"),
            new Competitor("Amgen", "Enbrel", 18.2, "US/EU", "1998", "Rheumatoid Arthritis", "Early entry"),
            new Competitor("Johnson & Johnson", "Remicade", 15.7, "Global", "1998", "Rheumatoid Arthritis", "Hospital focus"),
            new Competitor("Novartis", "Cosentyx", 8.9, "Global", "2015", "Psoriasis", "IL-17 targeting")
        );
        
        MarketData humiraMarket = new MarketData(
            HUMIRA, 18500, 6.8, "Global",
            Arrays.asList("Rheumatoid Arthritis", "Crohn's Disease", "Psoriasis", "Ankylosing Spondylitis"),
            Arrays.asList("Hidradenitis Suppurativa", "Uveitis", "Pediatric Crohn's"),
            humiraCompetitors,
            Arrays.asList("Aging population", "Improved diagnosis", "Expanding indications"),
            Arrays.asList("Biosimilar competition", "High cost", "Safety concerns"),
            "Strong growth despite biosimilar entry, driven by new indications",
            28750.0
        );
        marketDataMap.put(HUMIRA, humiraMarket);
    }
    
    private void initializePatentData() {
        // Montelukast Patents
        List<PatentInfo> montelukastPatents = Arrays.asList(
            new PatentInfo("US5565473", "Leukotriene antagonists", "Merck & Co.", "1994-06-15", 
                          "1996-10-15", "2014-06-15", "Expired", "US", MONTELUKAST, "Asthma", 
                          "Composition", "High", "Ronald Chand, et al."),
            new PatentInfo("EP0480717", "Quinoline leukotriene antagonists", "Merck & Co.", "1991-04-17", 
                          "1992-04-15", "2011-04-17", "Expired", "EU", MONTELUKAST, "Asthma", 
                          "Composition", "High", "Ronald Chand, et al."),
            new PatentInfo("US6268533", "Montelukast sodium salt forms", "Merck & Co.", "1999-03-12", 
                          "2001-07-31", "2019-03-12", "Expired", "US", MONTELUKAST, "Asthma", 
                          "Formulation", "Medium", "James Coe, et al.")
        );
        patentDataMap.put(MONTELUKAST, montelukastPatents);
        
        // Humira Patents
        List<PatentInfo> humiraPatents = Arrays.asList(
            new PatentInfo("US6090382", "Human antibodies that bind human TNFα", "Abbott Laboratories", 
                          "1999-07-09", "2000-07-18", "2018-12-31", "Expired", "US", HUMIRA, 
                          "Rheumatoid Arthritis", "Composition", "High", "Robert Ladner, et al."),
            new PatentInfo("EP1212422", "Human antibodies that bind human TNF alpha", "Abbott Laboratories", 
                          "1999-07-09", "2002-06-12", "2019-07-09", "Expired", "EU", HUMIRA, 
                          "Rheumatoid Arthritis", "Composition", "High", "Robert Ladner, et al."),
            new PatentInfo("US8916158", "Methods for treating psoriasis using adalimumab", "AbbVie Inc.", 
                          "2012-05-04", "2014-12-23", "2032-05-04", "Active", "US", HUMIRA, 
                          "Psoriasis", "Method", "High", "Roopal Thakkar, et al.")
        );
        patentDataMap.put(HUMIRA, humiraPatents);
    }  
  
    private void initializeClinicalTrialData() {
        // Montelukast Clinical Trials
        List<ClinicalTrial> montelukastTrials = Arrays.asList(
            new ClinicalTrial("NCT00000001", "Montelukast in Pediatric Asthma", "Phase III", "Completed", 
                             "Merck & Co.", MONTELUKAST, "Pediatric Asthma", 689, "2018-01-15", 
                             "2020-12-30", "Change in FEV1 from baseline", "Quality of life, exacerbations", 
                             "Interventional", "Randomized", "Double Blind", "US, Canada, EU"),
            new ClinicalTrial("NCT00000002", "Montelukast vs Placebo in Exercise-Induced Asthma", "Phase II", 
                             "Completed", "Academic Medical Center", MONTELUKAST, "Exercise-Induced Asthma", 
                             156, "2019-03-01", "2021-08-15", "Exercise tolerance improvement", 
                             "Lung function, symptoms", "Interventional", "Randomized", "Double Blind", "Netherlands"),
            new ClinicalTrial("NCT00000003", "Long-term Safety of Montelukast", "Phase IV", "Active, not recruiting", 
                             "Merck & Co.", MONTELUKAST, "Asthma", 2340, "2020-06-01", "2025-05-31", 
                             "Adverse events incidence", "Liver function, growth in children", 
                             "Observational", "Non-Randomized", "Open Label", "Global")
        );
        clinicalTrialDataMap.put(MONTELUKAST, montelukastTrials);
        
        // Humira Clinical Trials
        List<ClinicalTrial> humiraTrials = Arrays.asList(
            new ClinicalTrial("NCT00000004", "Adalimumab in Moderate to Severe Rheumatoid Arthritis", "Phase III", 
                             "Completed", "AbbVie", HUMIRA, "Rheumatoid Arthritis", 799, "2017-09-12", 
                             "2020-03-28", "ACR20 response at week 24", "ACR50, ACR70, radiographic progression", 
                             "Interventional", "Randomized", "Double Blind", "US, EU, Japan"),
            new ClinicalTrial("NCT00000005", "Humira in Pediatric Crohn's Disease", "Phase III", "Completed", 
                             "AbbVie", HUMIRA, "Pediatric Crohn's Disease", 192, "2018-11-05", "2021-09-20", 
                             "Clinical remission at week 26", "Mucosal healing, growth velocity", 
                             "Interventional", "Randomized", "Open Label", "US, Canada, EU"),
            new ClinicalTrial("NCT00000006", "Adalimumab Biosimilar Equivalence Study", "Phase III", "Recruiting", 
                             "Sandoz", HUMIRA, "Rheumatoid Arthritis", 600, "2023-02-15", "2025-12-31", 
                             "Pharmacokinetic equivalence", "Safety, immunogenicity", "Interventional", 
                             "Randomized", "Double Blind", "EU, Australia")
        );
        clinicalTrialDataMap.put(HUMIRA, humiraTrials);
        
        // Metformin Clinical Trials
        List<ClinicalTrial> metforminTrials = Arrays.asList(
            new ClinicalTrial("NCT00000007", "Metformin in Prediabetes Prevention", "Phase III", "Completed", 
                             "NIH/NIDDK", METFORMIN, "Prediabetes", 3234, "2016-01-10", "2019-12-15", 
                             "Progression to Type 2 diabetes", "Weight loss, cardiovascular events", 
                             "Interventional", "Randomized", "Double Blind", "US"),
            new ClinicalTrial("NCT00000008", "Extended Release Metformin vs Standard", "Phase II", "Completed", 
                             "Teva Pharmaceuticals", METFORMIN, "Type 2 Diabetes", 456, "2020-05-20", 
                             "2022-11-30", "HbA1c reduction", "Gastrointestinal tolerability", 
                             "Interventional", "Randomized", "Open Label", "US, Canada")
        );
        clinicalTrialDataMap.put(METFORMIN, metforminTrials);
        
        // GLP-1 Clinical Trials
        List<ClinicalTrial> glp1Trials = Arrays.asList(
            new ClinicalTrial("NCT00000009", "Semaglutide in Obesity Management", "Phase III", "Completed", 
                             "Novo Nordisk", GLP1, "Obesity", 1961, "2019-08-12", "2022-06-30", 
                             "Weight reduction ≥15%", "Cardiovascular outcomes, quality of life", 
                             "Interventional", "Randomized", "Double Blind", "Global"),
            new ClinicalTrial("NCT00000010", "Dulaglutide Cardiovascular Outcomes", "Phase III", "Completed", 
                             "Eli Lilly", GLP1, "Type 2 Diabetes", 9901, "2018-03-01", "2021-11-15", 
                             "Major cardiovascular events", "All-cause mortality, hospitalization", 
                             "Interventional", "Randomized", "Double Blind", "Global")
        );
        clinicalTrialDataMap.put(GLP1, glp1Trials);
        
        // Eliquis Clinical Trials
        List<ClinicalTrial> eliquiTrials = Arrays.asList(
            new ClinicalTrial("NCT00000011", "Apixaban vs Warfarin in Atrial Fibrillation", "Phase III", "Completed", 
                             "Bristol Myers Squibb", ELIQUIS, "Atrial Fibrillation", 18201, "2017-02-28", 
                             "2020-08-31", "Stroke and systemic embolism", "Major bleeding, all-cause mortality", 
                             "Interventional", "Randomized", "Double Blind", "Global"),
            new ClinicalTrial("NCT00000012", "Eliquis in Cancer-Associated Thrombosis", "Phase II", "Active, not recruiting", 
                             "Pfizer", ELIQUIS, "Cancer-Associated VTE", 576, "2022-01-15", "2024-12-31", 
                             "Recurrent VTE at 6 months", "Major bleeding, survival", "Interventional", 
                             "Randomized", "Open Label", "US, EU, Canada")
        );
        clinicalTrialDataMap.put(ELIQUIS, eliquiTrials);
    }
    
    private void initializeEximTradeData() {
        // Montelukast Trade Data
        List<EximTrade> montelukastTrade = Arrays.asList(
            new EximTrade(MONTELUKAST, "India", "Export", 125000.0, 15600000.0, "2024", "March", 
                         "2934.99", "Montelukast Sodium API", "United States", 124.8, 28.5, "+12.3%"),
            new EximTrade(MONTELUKAST, "China", "Export", 89000.0, 11200000.0, "2024", "March", 
                         "2934.99", "Montelukast Sodium", "Germany", 125.8, 20.4, "+8.7%"),
            new EximTrade(MONTELUKAST, "United States", "Import", 45000.0, 5850000.0, "2024", "March", 
                         "2934.99", "Montelukast API", "India", 130.0, 15.2, "+15.6%"),
            new EximTrade(MONTELUKAST, "Brazil", "Import", 32000.0, 4160000.0, "2024", "February", 
                         "2934.99", "Montelukast Tablets", "India", 130.0, 11.8, "+22.1%")
        );
        eximTradeDataMap.put(MONTELUKAST, montelukastTrade);
        
        // Humira Trade Data
        List<EximTrade> humiraTrade = Arrays.asList(
            new EximTrade(HUMIRA, "Ireland", "Export", 2500.0, 890000000.0, "2024", "March", 
                         "3002.90", "Adalimumab Injection", "United States", 356000.0, 45.2, "+6.8%"),
            new EximTrade(HUMIRA, "Germany", "Export", 1800.0, 625000000.0, "2024", "March", 
                         "3002.90", "Adalimumab Biosimilar", "United Kingdom", 347222.0, 31.7, "+18.9%"),
            new EximTrade(HUMIRA, "United States", "Import", 1200.0, 420000000.0, "2024", "February", 
                         "3002.90", "Adalimumab", "Ireland", 350000.0, 21.3, "-2.1%"),
            new EximTrade(HUMIRA, "Japan", "Import", 890.0, 298000000.0, "2024", "March", 
                         "3002.90", "Humira Injection", "Ireland", 334831.0, 15.1, "+4.2%")
        );
        eximTradeDataMap.put(HUMIRA, humiraTrade);
        
        // Metformin Trade Data
        List<EximTrade> metforminTrade = Arrays.asList(
            new EximTrade(METFORMIN, "India", "Export", 2800000.0, 168000000.0, "2024", "March", 
                         "2942.00", "Metformin HCl API", "United States", 60.0, 35.8, "+9.2%"),
            new EximTrade(METFORMIN, "China", "Export", 2100000.0, 115500000.0, "2024", "March", 
                         "2942.00", "Metformin HCl", "Brazil", 55.0, 24.7, "+11.5%"),
            new EximTrade(METFORMIN, "United States", "Import", 1500000.0, 93000000.0, "2024", "February", 
                         "2942.00", "Metformin API", "India", 62.0, 19.9, "+7.8%"),
            new EximTrade(METFORMIN, "Germany", "Import", 890000.0, 53400000.0, "2024", "March", 
                         "2942.00", "Metformin Tablets", "India", 60.0, 11.4, "+13.2%")
        );
        eximTradeDataMap.put(METFORMIN, metforminTrade);
        
        // GLP-1 Trade Data
        List<EximTrade> glp1Trade = Arrays.asList(
            new EximTrade(GLP1, "Denmark", "Export", 450.0, 2250000000.0, "2024", "March", 
                         "3001.90", "Semaglutide Injection", "United States", 5000000.0, 52.3, "+28.7%"),
            new EximTrade(GLP1, "United States", "Export", 320.0, 1440000000.0, "2024", "March", 
                         "3001.90", "Liraglutide", "Germany", 4500000.0, 33.5, "+19.4%"),
            new EximTrade(GLP1, "Germany", "Import", 180.0, 810000000.0, "2024", "February", 
                         "3001.90", "GLP-1 Agonist", "Denmark", 4500000.0, 18.8, "+31.2%"),
            new EximTrade(GLP1, "Japan", "Import", 125.0, 562500000.0, "2024", "March", 
                         "3001.90", "Dulaglutide", "United States", 4500000.0, 13.1, "+25.6%")
        );
        eximTradeDataMap.put(GLP1, glp1Trade);
        
        // Eliquis Trade Data
        List<EximTrade> eliquisTrade = Arrays.asList(
            new EximTrade(ELIQUIS, "Ireland", "Export", 1200.0, 720000000.0, "2024", "March", 
                         "3004.90", "Apixaban Tablets", "United States", 600000.0, 41.2, "+14.8%"),
            new EximTrade(ELIQUIS, "Germany", "Export", 890.0, 498200000.0, "2024", "March", 
                         "3004.90", "Apixaban API", "Japan", 559775.0, 28.5, "+8.9%"),
            new EximTrade(ELIQUIS, "United States", "Import", 650.0, 390000000.0, "2024", "February", 
                         "3004.90", "Eliquis Tablets", "Ireland", 600000.0, 22.3, "+12.1%"),
            new EximTrade(ELIQUIS, "Canada", "Import", 340.0, 204000000.0, "2024", "March", 
                         "3004.90", "Apixaban", "Ireland", 600000.0, 11.7, "+16.7%")
        );
        eximTradeDataMap.put(ELIQUIS, eliquisTrade);
    }    

    private void initializePublicationData() {
        // Montelukast Publications
        List<Publication> montelukastPubs = Arrays.asList(
            new Publication("Efficacy and safety of montelukast in pediatric asthma: a systematic review", 
                           "Smith J, Johnson A, Brown K", "Pediatric Pulmonology", "2023-08-15", 
                           "10.1002/ppul.25892", "37584123", 
                           "This systematic review evaluates the efficacy and safety of montelukast in pediatric asthma management...", 
                           MONTELUKAST, "Pediatric Asthma", "Review", "montelukast, pediatric, asthma, leukotriene", 
                           45, "3.2", "https://pubmed.ncbi.nlm.nih.gov/37584123/", "High"),
            new Publication("Long-term effects of montelukast on exercise-induced bronchoconstriction", 
                           "Davis M, Wilson R, Taylor S", "Journal of Asthma", "2023-06-20", 
                           "10.1080/02770903.2023.2234567", "37345678", 
                           "A longitudinal study examining the sustained effects of montelukast therapy on exercise tolerance...", 
                           MONTELUKAST, "Exercise-Induced Asthma", "Clinical", "exercise, bronchoconstriction, montelukast", 
                           28, "2.8", "https://pubmed.ncbi.nlm.nih.gov/37345678/", "High"),
            new Publication("Montelukast resistance mechanisms in severe asthma patients", 
                           "Garcia L, Martinez P, Rodriguez C", "Respiratory Medicine", "2023-09-10", 
                           "10.1016/j.rmed.2023.107234", "37698765", 
                           "Investigation of molecular mechanisms underlying montelukast resistance in treatment-refractory asthma...", 
                           MONTELUKAST, "Severe Asthma", "Preclinical", "resistance, leukotriene receptor, genetics", 
                           12, "4.1", "https://pubmed.ncbi.nlm.nih.gov/37698765/", "Medium")
        );
        publicationDataMap.put(MONTELUKAST, montelukastPubs);
        
        // Humira Publications
        List<Publication> humiraPubs = Arrays.asList(
            new Publication("Real-world effectiveness of adalimumab in rheumatoid arthritis: 5-year follow-up", 
                           "Anderson K, Thompson L, White M", "Rheumatology", "2023-07-25", 
                           "10.1093/rheumatology/keab456", "37456789", 
                           "Long-term real-world study demonstrating sustained efficacy of adalimumab in RA patients...", 
                           HUMIRA, "Rheumatoid Arthritis", "Clinical", "adalimumab, real-world, effectiveness, RA", 
                           89, "5.4", "https://pubmed.ncbi.nlm.nih.gov/37456789/", "High"),
            new Publication("Biosimilar adalimumab: comprehensive safety and efficacy analysis", 
                           "Kumar S, Patel N, Lee H", "BioDrugs", "2023-09-05", 
                           "10.1007/s40259-023-00612-3", "37654321", 
                           "Systematic analysis of biosimilar adalimumab products across multiple therapeutic areas...", 
                           HUMIRA, "Multiple Indications", "Review", "biosimilar, adalimumab, safety, efficacy", 
                           67, "4.8", "https://pubmed.ncbi.nlm.nih.gov/37654321/", "High"),
            new Publication("Adalimumab in pediatric inflammatory bowel disease: growth outcomes", 
                           "Chen W, Liu X, Zhang Y", "Journal of Pediatric Gastroenterology", "2023-08-30", 
                           "10.1097/MPG.0000000000003789", "37567890", 
                           "Analysis of growth velocity and final height in pediatric IBD patients treated with adalimumab...", 
                           HUMIRA, "Pediatric IBD", "Clinical", "pediatric, growth, inflammatory bowel disease", 
                           34, "3.9", "https://pubmed.ncbi.nlm.nih.gov/37567890/", "Medium")
        );
        publicationDataMap.put(HUMIRA, humiraPubs);
        
        // Metformin Publications
        List<Publication> metforminPubs = Arrays.asList(
            new Publication("Metformin and cardiovascular outcomes in type 2 diabetes: updated meta-analysis", 
                           "Johnson R, Williams T, Davis A", "Diabetes Care", "2023-09-12", 
                           "10.2337/dc23-1234", "37712345", 
                           "Updated meta-analysis of cardiovascular outcomes with metformin therapy in T2DM patients...", 
                           METFORMIN, "Type 2 Diabetes", "Review", "metformin, cardiovascular, meta-analysis", 
                           156, "17.8", "https://pubmed.ncbi.nlm.nih.gov/37712345/", "High"),
            new Publication("Metformin mechanisms in cancer prevention: molecular insights", 
                           "Brown S, Miller J, Wilson K", "Nature Reviews Drug Discovery", "2023-08-18", 
                           "10.1038/s41573-023-00789-1", "37598765", 
                           "Comprehensive review of metformin's anti-cancer mechanisms and clinical implications...", 
                           METFORMIN, "Cancer Prevention", "Review", "metformin, cancer, AMPK, mechanisms", 
                           203, "75.9", "https://pubmed.ncbi.nlm.nih.gov/37598765/", "High")
        );
        publicationDataMap.put(METFORMIN, metforminPubs);
        
        // GLP-1 Publications
        List<Publication> glp1Pubs = Arrays.asList(
            new Publication("Semaglutide for weight management: cardiovascular safety profile", 
                           "Taylor M, Roberts P, Clark D", "New England Journal of Medicine", "2023-09-20", 
                           "10.1056/NEJMoa2345678", "37823456", 
                           "Comprehensive cardiovascular safety analysis of semaglutide in obesity management...", 
                           GLP1, "Obesity", "Clinical", "semaglutide, cardiovascular safety, weight loss", 
                           278, "176.1", "https://pubmed.ncbi.nlm.nih.gov/37823456/", "High"),
            new Publication("GLP-1 receptor agonists in diabetic kidney disease: systematic review", 
                           "Singh A, Patel R, Kumar V", "Kidney International", "2023-08-08", 
                           "10.1016/j.kint.2023.07.012", "37534567", 
                           "Systematic evaluation of GLP-1 agonists' renoprotective effects in diabetic nephropathy...", 
                           GLP1, "Diabetic Kidney Disease", "Review", "GLP-1, kidney disease, diabetes, nephropathy", 
                           92, "19.6", "https://pubmed.ncbi.nlm.nih.gov/37534567/", "High")
        );
        publicationDataMap.put(GLP1, glp1Pubs);
        
        // Eliquis Publications
        List<Publication> eliquisPubs = Arrays.asList(
            new Publication("Apixaban vs warfarin in atrial fibrillation: real-world bleeding outcomes", 
                           "Martinez L, Garcia F, Lopez R", "Circulation", "2023-09-15", 
                           "10.1161/CIRCULATIONAHA.123.065432", "37789012", 
                           "Large-scale real-world comparison of bleeding outcomes between apixaban and warfarin...", 
                           ELIQUIS, "Atrial Fibrillation", "Clinical", "apixaban, warfarin, bleeding, real-world", 
                           134, "29.7", "https://pubmed.ncbi.nlm.nih.gov/37789012/", "High"),
            new Publication("Apixaban reversal strategies in emergency surgery: clinical guidelines", 
                           "Thompson K, White J, Brown L", "Journal of Thrombosis and Haemostasis", "2023-07-30", 
                           "10.1111/jth.15234", "37445678", 
                           "Evidence-based guidelines for apixaban reversal in emergency surgical procedures...", 
                           ELIQUIS, "Emergency Surgery", "Review", "apixaban, reversal, emergency, surgery", 
                           56, "6.1", "https://pubmed.ncbi.nlm.nih.gov/37445678/", "Medium")
        );
        publicationDataMap.put(ELIQUIS, eliquisPubs);
        
        // Complete remaining market data
        completeMarketDataInitialization();
    }
    
    private void completeMarketDataInitialization() {
        // Metformin Market Data
        List<Competitor> metforminCompetitors = Arrays.asList(
            new Competitor("Teva Pharmaceuticals", "Metformin HCl", 22.1, "Global", "2002", "Type 2 Diabetes", "Generic leader"),
            new Competitor("Mylan", "Metformin ER", 18.7, "US/EU", "2003", "Type 2 Diabetes", "Extended release"),
            new Competitor("Sandoz", "Metformin", 15.3, "Global", "2004", "Type 2 Diabetes", "Cost efficiency"),
            new Competitor("Aurobindo", "Metformin HCl", 12.9, "US/India", "2008", "Type 2 Diabetes", "Manufacturing scale")
        );
        
        MarketData metforminMarket = new MarketData(
            METFORMIN, 1850, 3.8, "Global",
            Arrays.asList("Type 2 Diabetes", "Prediabetes", "PCOS"),
            Arrays.asList("Weight Management", "Anti-aging", "Cancer Prevention"),
            metforminCompetitors,
            Arrays.asList("Rising diabetes prevalence", "Cost-effectiveness", "Multiple indications"),
            Arrays.asList("GLP-1 competition", "Side effects", "Contraindications"),
            "Stable growth with emerging applications beyond diabetes",
            2420.0
        );
        marketDataMap.put(METFORMIN, metforminMarket);
        
        // GLP-1 Market Data
        List<Competitor> glp1Competitors = Arrays.asList(
            new Competitor("Novo Nordisk", "Ozempic/Wegovy", 42.3, "Global", "2017", "Diabetes/Obesity", "Market leader"),
            new Competitor("Eli Lilly", "Trulicity/Mounjaro", 28.9, "Global", "2014", "Diabetes", "Dual agonist"),
            new Competitor("Sanofi", "Soliqua", 12.4, "US/EU", "2016", "Diabetes", "Combination therapy"),
            new Competitor("AstraZeneca", "Bydureon", 8.7, "Global", "2012", "Diabetes", "Weekly dosing")
        );
        
        MarketData glp1Market = new MarketData(
            GLP1, 22400, 15.2, "Global",
            Arrays.asList("Type 2 Diabetes", "Obesity", "Cardiovascular Risk Reduction"),
            Arrays.asList("NASH", "Alzheimer's Disease", "Addiction Treatment"),
            glp1Competitors,
            Arrays.asList("Obesity epidemic", "Cardiovascular benefits", "Weight loss efficacy"),
            Arrays.asList("High cost", "Supply constraints", "Side effects"),
            "Explosive growth driven by obesity indication and pipeline expansion",
            58750.0
        );
        marketDataMap.put(GLP1, glp1Market);
        
        // Eliquis Market Data
        List<Competitor> eliquiCompetitors = Arrays.asList(
            new Competitor("Bristol Myers Squibb/Pfizer", "Eliquis", 38.7, "Global", "2011", "Atrial Fibrillation", "Safety profile"),
            new Competitor("Bayer/J&J", "Xarelto", 32.1, "Global", "2011", "Atrial Fibrillation", "Once daily dosing"),
            new Competitor("Boehringer Ingelheim", "Pradaxa", 18.9, "Global", "2010", "Atrial Fibrillation", "First DOAC"),
            new Competitor("Daiichi Sankyo", "Savaysa", 6.8, "US/Japan", "2015", "Atrial Fibrillation", "Regional focus")
        );
        
        MarketData eliquiMarket = new MarketData(
            ELIQUIS, 9850, 7.3, "Global",
            Arrays.asList("Atrial Fibrillation", "Venous Thromboembolism", "Post-surgical Prophylaxis"),
            Arrays.asList("Cancer-Associated VTE", "Pediatric Thrombosis"),
            eliquiCompetitors,
            Arrays.asList("Aging population", "AF prevalence increase", "Surgery volume growth"),
            Arrays.asList("Generic competition", "Bleeding risks", "Reversal agents"),
            "Continued growth with expanding indications and aging demographics",
            14200.0
        );
        marketDataMap.put(ELIQUIS, eliquiMarket);
    }
}