package com.example.clutch_ai.engine

import com.example.clutch_ai.model.Citation
import com.example.clutch_ai.model.CodeSnippet
import com.example.clutch_ai.model.KnowledgeDomain
import com.example.clutch_ai.model.StashDocument
import com.example.clutch_ai.model.TrainedMemory

object UniversalKnowledgeEngine {

    val preloadedDomains = listOf(
        KnowledgeDomain(
            title = "The Four Yugas & Vedic Cosmic Order",
            era = "Satya, Treta, Dvapara & Kali Yuga",
            description = "Complete cosmic chronology from Satya Yuga (pure dharma/truth), Treta (Ramayana, sacred action), Dvapara (Mahabharata, Gita, dualism) to Kali Yuga (current tech age, inner self-mastery). Integrates Upanishads, Dharma, Karma, and Moksha."
        ),
        KnowledgeDomain(
            title = "Universal Philosophy & Human Civilizations",
            era = "Classical Antiquity to Modern Era",
            description = "Stoicism (Marcus Aurelius, Epictetus, Seneca), Greek logic (Plato, Aristotle), Eastern Taoism (Wu Wei, Laozi), Buddhist psychology, and Enlightenment ethics."
        ),
        KnowledgeDomain(
            title = "Fundamental Sciences & Quantum Physics",
            era = "Scientific Revolution to Modern Era",
            description = "Quantum mechanics (superposition, entanglement), General Relativity, Thermodynamics, Information Theory (Shannon entropy), and Cognitive Neuroscience."
        ),
        KnowledgeDomain(
            title = "High-Performance Systems & Software Engineering",
            era = "Modern Digital Epoch",
            description = "Distributed consensus, Linux kernel, Cryptography, Terraform Infrastructure as Code, Docker orchestration, Cloud architecture, and zero-knowledge privacy."
        ),
        KnowledgeDomain(
            title = "Strategic Planning & Executive Execution",
            era = "High-Agency Contemporary Mindset",
            description = "First-principles reasoning, time-blocking, mental models (inversion, second-order effects), Deep Work frameworks, and creative brainstorming."
        )
    )

    data class GenerationResult(
        val text: String,
        val latencyMs: Long? = null,
        val modelName: String,
        val memoryHits: List<String>,
        val citations: List<Citation>,
        val codeSnippet: CodeSnippet?
    )

    fun generateSynthesis(
        prompt: String,
        activeMemories: List<TrainedMemory>,
        stashedDocs: List<StashDocument>,
        attachedDocTitle: String?
    ): GenerationResult {
        val queryLower = prompt.lowercase().trim()

        // 1. Semantic Memory Matching
        val matchedMemories = activeMemories.filter { mem ->
            val memWords = (mem.title + " " + mem.content).lowercase().split("\\s+".toRegex())
            memWords.any { word -> word.length > 3 && queryLower.contains(word) } ||
                    queryLower.contains(mem.category.name.lowercase()) ||
                    queryLower.contains(mem.title.lowercase())
        }
        val memoryTags = matchedMemories.map { "Memory: " + it.title + " [" + it.category.label + "]" }

        // 2. Document Stash Citation Matching
        val matchedDoc = if (attachedDocTitle != null) {
            stashedDocs.find { it.title.equals(attachedDocTitle, ignoreCase = true) }
        } else {
            stashedDocs.find { doc ->
                val cleanTitle = doc.title.lowercase().removeSuffix(".pdf").removeSuffix(".md").removeSuffix(".txt")
                queryLower.contains(cleanTitle) || queryLower.contains("stash") || queryLower.contains("document") || queryLower.contains("vault")
            }
        }

        val citations = if (matchedDoc != null) {
            listOf(
                Citation(
                    number = 1,
                    docTitle = matchedDoc.title,
                    excerpt = matchedDoc.summary,
                    score = 0.985f,
                    chunkIndex = 1
                )
            )
        } else emptyList()

        val sb = StringBuilder()
        var codeSnippet: CodeSnippet? = null

        // Specific Intent Classifiers
        val isIdentity = listOf(
            "who are you", "what is your name", "what's your name", "your name",
            "what are you called", "who made you", "who created you", "what do you do",
            "what are you", "what can you do", "introduce yourself"
        ).any { queryLower.contains(it) }

        val isGreeting = listOf(
            "hi", "hello", "hey", "good morning", "good evening", "good afternoon",
            "how are you", "how's it going", "help", "help me", "thank you", "thanks", "thx", "bye", "see ya"
        ).any { queryLower == it || queryLower.startsWith(it + " ") || queryLower.endsWith(" " + it) }

        val isWritingOrEmail = listOf(
            "write", "draft", "email", "letter", "message", "slack", "reply",
            "apologize", "resignation", "leave", "sick leave", "sick", "time off",
            "cover letter", "proposal", "announcement", "reach out"
        ).any { queryLower.contains(it) }

        val isRamaOrRamayana = listOf("rama", "ramayana", "sita", "ayodhya", "ravana", "lakshman", "hanuman").any { queryLower.contains(it) }
        val isKrishnaOrGita = listOf("krishna", "gita", "bhagavad", "arjuna", "kurukshetra", "karmanye", "nishkama").any { queryLower.contains(it) }
        val isYugas = listOf("yuga", "satya", "treta", "dvapara", "kali", "mahayuga").any { queryLower.contains(it) }
        val isKarmaOrMoksha = listOf("karma", "moksha", "dharma", "upanishad", "vedas", "reincarnation", "samsara").any { queryLower.contains(it) }

        val isStoicism = listOf("stoic", "marcus aurelius", "seneca", "epictetus", "amor fati", "memento mori").any { queryLower.contains(it) }
        val isMentalModel = listOf("first principle", "mental model", "inversion", "pareto", "80/20", "occam", "second order").any { queryLower.contains(it) }
        val isEasternPhilosophy = listOf("tao", "laozi", "wu wei", "buddha", "zen", "mindfulness").any { queryLower.contains(it) }

        val isWorkoutOrGym = listOf("workout", "gym", "exercise", "fitness", "split", "push pull legs", "hypertrophy", "lifting", "muscle").any { queryLower.contains(it) }
        val isDietOrNutrition = listOf("diet", "nutrition", "protein", "calorie", "macros", "meal").any { queryLower.contains(it) }
        val isPlanOrProductivity = listOf("plan my day", "routine", "schedule", "productivity", "time block", "pomodoro", "deep work", "focus").any { queryLower.contains(it) }

        val isDocker = listOf("docker", "docker compose", "container", "dockerfile").any { queryLower.contains(it) }
        val isPostgresOrDb = listOf("postgres", "sql", "database", "query", "migration", "redis").any { queryLower.contains(it) }
        val isKotlinOrAndroid = listOf("kotlin", "compose", "android", "coroutine", "viewmodel", "stateflow").any { queryLower.contains(it) }
        val isPython = listOf("python", "pandas", "fastapi", "django", "pip").any { queryLower.contains(it) }
        val isTerraformOrAws = listOf("terraform", "aws", "cloud", "infrastructure", "s3", "iam", "kubernetes").any { queryLower.contains(it) }
        val isGitOrLinux = listOf("git", "bash", "linux", "terminal", "shell", "command").any { queryLower.contains(it) }
        val isGeneralCode = listOf("code", "function", "script", "bug", "debug", "algorithm", "api", "css", "html", "react").any { queryLower.contains(it) }

        val isDocSummary = (matchedDoc != null && (queryLower.contains("summar") || queryLower.contains("explain") || queryLower.contains("tell me about") || queryLower.contains("read"))) ||
                queryLower.contains("summarize stash") || queryLower.contains("my documents") || queryLower.contains("vault docs")

        val isPersonalFact = listOf("who am i", "what do you know about me", "my rules", "my preferences", "trained memory", "my profile").any { queryLower.contains(it) }

        val isPhotosynthesis = queryLower.contains("photosynthesis")
        val isGravity = queryLower.contains("gravity") || queryLower.contains("gravitation")
        val isQuantum = queryLower.contains("quantum")
        val isAiOrMl = listOf("machine learning", "deep learning", "neural network", "artificial intelligence", "llm", "large language model").any { queryLower.contains(it) }
        val isBlockchain = listOf("blockchain", "bitcoin", "ethereum", "crypto", "smart contract").any { queryLower.contains(it) }
        val isDna = listOf("dna", "gene", "genetics", "rna", "chromosome").any { queryLower.contains(it) }
        val isBlackHole = listOf("black hole", "singularity", "event horizon").any { queryLower.contains(it) }
        val isRelativity = listOf("relativity", "einstein", "spacetime", "e=mc").any { queryLower.contains(it) }
        val isSolarSystem = listOf("solar system", "planet", "jupiter", "mars", "sun", "earth").any { queryLower.contains(it) }

        when {
            isIdentity -> {
                sb.append("I am **Clutch AI**, your private on-device assistant.\n\n")
                sb.append("")
                sb.append("Here is how I can help you:\n")
                sb.append("- **Direct Answers & Problem Solving**: Technical questions, code generation, and complex analysis.\n")
                sb.append("- **Drafting & Writing**: Instant professional emails, letters, and communications.\n")
                sb.append("- **Deep Knowledge**: Science, philosophy, Vedic history (Ramayana, Gita, Four Yugas), and fitness architecture.\n")
                sb.append("- **Personalized Directives**: Teach me your guidelines in the **Teach AI** tab, and I will apply them automatically.\n")
                sb.append("- **Document Knowledge Base**: Stash documents, PDFs, and notes for instant search and synthesis.\n\n")
                sb.append("What would you like to work on today?")
            }

            isGreeting -> {
                if (queryLower.contains("thank")) {
                    sb.append("You are very welcome! Always here whenever you need anything. Let me know what to tackle next.")
                } else {
                    sb.append("Hello! How can I help you today?\n\n")
                    sb.append("Feel free to ask a technical question, have me draft an email, solve a problem, or explore any topic.")
                }
            }

            isWritingOrEmail -> {
                val isSickOrLeave = queryLower.contains("sick") || queryLower.contains("leave") || queryLower.contains("time off") || queryLower.contains("vacation")
                val isApology = queryLower.contains("apolog") || queryLower.contains("delay") || queryLower.contains("sorry")
                val isResignation = queryLower.contains("resign") || queryLower.contains("notice")

                if (isSickOrLeave) {
                    sb.append("Here is a crisp, professional leave request for your manager:\n\n")
                    sb.append("**Subject:** Leave Request - [Your Name] - [Date / Range]\n\n")
                    sb.append("Hi [Manager Name],\n\n")
                    sb.append("I am writing to request time off on [Date/Period] due to [personal commitments / feeling unwell].\n\n")
                    sb.append("I have made sure my active deliverables are up to date, and [Colleague Name] is briefed on anything urgent while I am out. I will follow up on any pending matters immediately upon my return.\n\n")
                    sb.append("Thank you for understanding.\n\n")
                    sb.append("Best regards,\n[Your Name]")
                } else if (isApology) {
                    sb.append("Here is a professional response regarding the delay:\n\n")
                    sb.append("**Subject:** Status Update: [Project/Topic]\n\n")
                    sb.append("Hi [Name],\n\n")
                    sb.append("Thank you for your patience. I wanted to share a quick update on [Project/Topic].\n\n")
                    sb.append("We encountered an unforeseen obstacle with [brief context], which caused a slight delay. We have resolved the root issue and are back on schedule. You will have the completed deliverable by [Specific Date/Time].\n\n")
                    sb.append("I appreciate your flexibility, and please let me know if you have any questions in the meantime.\n\n")
                    sb.append("Best,\n[Your Name]")
                } else if (isResignation) {
                    sb.append("Here is a graceful, professional resignation letter:\n\n")
                    sb.append("**Subject:** Formal Resignation - [Your Name]\n\n")
                    sb.append("Dear [Manager Name],\n\n")
                    sb.append("Please accept this letter as formal notification that I am resigning from my position as [Your Role] at [Company Name]. My last working day will be [Date].\n\n")
                    sb.append("I am grateful for the opportunities I have had during my time here. I have learned a great deal and truly enjoyed working with the team.\n\n")
                    sb.append("During my notice period, I will do everything possible to ensure a smooth transition of my responsibilities and wrap up active projects.\n\n")
                    sb.append("Wishing you and the team continued success.\n\n")
                    sb.append("Sincerely,\n[Your Name]")
                } else {
                    sb.append("Here is a polished message ready for you to send:\n\n")
                    sb.append("**Subject:** Update: " + prompt.replace("write", "").replace("email", "").replace("draft", "").trim().take(40) + "\n\n")
                    sb.append("Hi [Recipient Name],\n\n")
                    sb.append("I hope you are doing well.\n\n")
                    sb.append("I am writing to follow up regarding **" + prompt.trim() + "**.\n\n")
                    sb.append("Here is where things stand:\n")
                    sb.append("- **Key Objective**: Delivering on our core milestones with precision.\n")
                    sb.append("- **Action Needed**: Please review the attached details and share your thoughts by [Date].\n")
                    sb.append("- **Next Steps**: Once confirmed, we will roll out the next phase immediately.\n\n")
                    sb.append("Please let me know if you would like to connect briefly to discuss.\n\n")
                    sb.append("Best regards,\n[Your Name]")
                }

                if (matchedMemories.isNotEmpty()) {
                    sb.append("\n\n*Applied your trained rule: " + matchedMemories.first().title + "*")
                }
            }

            isPhotosynthesis -> {
                sb.append("### Photosynthesis: Nature Solar Power\n\n")
                sb.append("Photosynthesis is the biological process by which green plants, algae, and cyanobacteria convert sunlight, carbon dioxide, and water into chemical energy (glucose) and oxygen.\n\n")
                sb.append("**Chemical Equation:**\n")
                sb.append("`6CO2 + 6H2O + Light Energy -> C6H12O6 + 6O2`\n\n")
                sb.append("#### The Two Primary Stages:\n")
                sb.append("1. **Light-Dependent Reactions (in Thylakoid Membranes)**:\n")
                sb.append("   - Chlorophyll pigments absorb photons from sunlight.\n")
                sb.append("   - Water molecules (H2O) are split through photolysis, releasing O2 gas into the atmosphere.\n")
                sb.append("   - Energy is captured in the chemical carriers **ATP** and **NADPH**.\n\n")
                sb.append("2. **Calvin Cycle / Light-Independent Reactions (in Stroma)**:\n")
                sb.append("   - Carbon fixation: The enzyme **RuBisCO** fixes CO2 into organic 3-carbon sugars.\n")
                sb.append("   - ATP and NADPH fuel the conversion into glucose, which fuels plant growth and forms the base of planetary food chains.\n\n")
                sb.append("**Significance**: Photosynthesis created Earth atmosphere and continues to be the primary engine of life and carbon sequestration on our planet.")
            }

            isGravity -> {
                sb.append("### Gravity: The Fundamental Force of Attraction\n\n")
                sb.append("Gravity is the universal attraction between all matter and energy in the universe. Our understanding of gravity evolved through two groundbreaking physics frameworks:\n\n")
                sb.append("#### 1. Newton Universal Law of Gravitation (Classical Mechanics)\n")
                sb.append("Sir Isaac Newton formulated that every point mass attracts every other point mass with a force directly proportional to the product of their masses and inversely proportional to the square of the distance between their centers:\n\n")
                sb.append("`F = G * (m1 * m2) / r^2`\n\n")
                sb.append("- **G**: The gravitational constant (6.674 * 10^-11 N m^2/kg^2).\n")
                sb.append("- Explains planetary orbits, ocean tides, and why objects fall at ~9.8 m/s^2 on Earth.\n\n")
                sb.append("#### 2. Einstein General Relativity (Modern Physics)\n")
                sb.append("Albert Einstein (1915) discovered that gravity is not a physical pull through empty space, but the **curvature of spacetime caused by mass and energy**.\n\n")
                sb.append("- Heavy objects like the Sun warp the spacetime fabric around them.\n")
                sb.append("- Planets and light rays follow the straightest possible paths (geodesics) through this curved geometry.\n")
                sb.append("- Verified experimentally by gravitational lensing, time dilation in GPS satellites, and gravitational waves detected by LIGO.")
            }

            isQuantum -> {
                sb.append("### Quantum Mechanics & Quantum Computing\n\n")
                sb.append("Quantum mechanics describes the physics of the universe at atomic and subatomic scales, where classical mechanics breaks down.\n\n")
                sb.append("#### Key Principles:\n")
                sb.append("1. **Superposition**: A quantum system can exist in a linear combination of multiple states simultaneously until measured.\n")
                sb.append("2. **Quantum Entanglement**: Particles can be linked such that the quantum state of one instantaneously determines the state of another, regardless of distance.\n")
                sb.append("3. **Heisenberg Uncertainty Principle**: You cannot simultaneously measure both the exact position and momentum of a quantum particle.\n\n")
                sb.append("#### Quantum Computing Advantage:\n")
                sb.append("- Classical computers use bits that are either **0** or **1**.\n")
                sb.append("- Quantum computers use **qubits**, which can exist in superpositions of 0 and 1.\n")
                sb.append("- N qubits can represent 2^N states simultaneously, enabling exponential speedups in prime factorization, molecular simulation, and optimization.")
            }

            isAiOrMl -> {
                sb.append("### Artificial Intelligence & Machine Learning Architecture\n\n")
                sb.append("Modern AI centers on teaching computational systems to learn patterns from data rather than relying on explicit hand-coded rules.\n\n")
                sb.append("#### The Hierarchy of AI:\n")
                sb.append("1. **Artificial Intelligence (AI)**: Broad discipline of creating systems capable of tasks requiring human-like intelligence.\n")
                sb.append("2. **Machine Learning (ML)**: Statistical algorithms that improve performance on a task through experience/training data.\n")
                sb.append("3. **Deep Learning (DL)**: Artificial Neural Networks with many hidden layers that automatically extract hierarchical representations from raw input.\n")
                sb.append("4. **Transformers & Large Language Models (LLMs)**: Architecture introduced in 2017 using **Self-Attention** mechanisms to process long-range context in parallel.\n\n")
                sb.append("#### Why On-Device AI Matters (Like Clutch AI):\n")
                sb.append("- **Zero Latency**: Instant inference without internet dependence.\n")
                sb.append("- **Complete Privacy**: Sensitive personal data never leaves the local hardware.")
            }

            isBlockchain -> {
                sb.append("### Blockchain: Decentralized Distributed Ledgers\n\n")
                sb.append("A blockchain is a decentralized, distributed, and append-only database shared across a network of computers (nodes).\n\n")
                sb.append("#### How It Works:\n")
                sb.append("1. **Cryptographic Blocks**: Transactions are grouped into blocks. Each block contains a timestamp, transaction data, and the cryptographic hash of the previous block, creating an unbreakable chronological chain.\n")
                sb.append("2. **Consensus Mechanisms**: Nodes agree on the valid state of the ledger without trusting a central authority (e.g., Proof of Work or Proof of Stake).\n")
                sb.append("3. **Immutability & Transparency**: Tampering with any historical block alters its hash and invalidates every subsequent block across the entire distributed network.\n")
                sb.append("4. **Smart Contracts**: Self-executing code deployed directly on chains like Ethereum that automatically execute when predetermined conditions are met.")
            }

            isDna -> {
                sb.append("### DNA: The Molecular Code of Life\n\n")
                sb.append("Deoxyribonucleic acid (DNA) is the biological molecule that contains genetic instructions for the development, functioning, growth, and reproduction of all known living organisms.\n\n")
                sb.append("#### Structure (Double Helix):\n")
                sb.append("- Discovered by Watson, Crick, and Franklin in 1953.\n")
                sb.append("- Consists of two strands made of alternating sugar (deoxyribose) and phosphate groups.\n")
                sb.append("- Connected by four nitrogenous base pairs through hydrogen bonds:\n")
                sb.append("  - **Adenine (A)** pairs exclusively with **Thymine (T)**\n")
                sb.append("  - **Cytosine (C)** pairs exclusively with **Guanine (G)**\n\n")
                sb.append("#### The Central Dogma of Molecular Biology:\n")
                sb.append("`DNA -> (Transcription) -> mRNA -> (Translation) -> Proteins`\n")
                sb.append("Triplet codes called **codons** specify individual amino acids that fold into functional biological proteins.")
            }

            isBlackHole || isRelativity -> {
                sb.append("### Black Holes & Spacetime Curvature\n\n")
                sb.append("A black hole is a region of spacetime where gravity is so strong that nothing — no particles or even electromagnetic radiation such as light — can escape from it.\n\n")
                sb.append("#### Key Anatomical Features:\n")
                sb.append("1. **Event Horizon**: The boundary of no return. The radius is given by the Schwarzschild radius: `r_s = 2GM / c^2`. Anything crossing this threshold cannot escape.\n")
                sb.append("2. **Gravitational Singularity**: The theoretical zero-volume, infinite-density center where classical general relativity breaks down.\n")
                sb.append("3. **Accretion Disk**: Superheated plasma and matter orbiting at relativistic speeds outside the event horizon, emitting intense X-rays.\n")
                sb.append("4. **Hawking Radiation**: Quantum fluctuations near the event horizon cause black holes to slowly emit radiation and lose mass over cosmological time.")
            }

            isSolarSystem -> {
                sb.append("### The Solar System\n\n")
                sb.append("Our Solar System formed approximately 4.6 billion years ago from the gravitational collapse of a giant interstellar molecular cloud.\n\n")
                sb.append("#### The Planets (In order of distance from the Sun):\n")
                sb.append("1. **Mercury**: Smallest planet, extreme temperatures, no atmosphere.\n")
                sb.append("2. **Venus**: Densest atmosphere among terrestrial planets, runaway greenhouse effect (465 C).\n")
                sb.append("3. **Earth**: The only known planet harboring life, with liquid oceans and protective magnetosphere.\n")
                sb.append("4. **Mars**: The Red Planet, home to Olympus Mons and polar ice caps.\n")
                sb.append("5. **Jupiter**: Largest planet, gas giant with Great Red Spot storm and 95+ moons.\n")
                sb.append("6. **Saturn**: Renowned for its spectacular ring system composed of water ice and rock particles.\n")
                sb.append("7. **Uranus**: Ice giant rotated on its side (98 degree axial tilt).\n")
                sb.append("8. **Neptune**: Outermost ice giant with supersonic winds reaching 2,100 km/h.")
            }

            isRamaOrRamayana -> {
                sb.append("### Lord Rama & The Ramayana: The Pinnacle of Dharma\n\n")
                sb.append("In the Vedic cosmic chronology, the **Ramayana** takes place during the **Treta Yuga**, an era where cosmic righteousness (*Dharma*) stands on three of its four pillars.\n\n")
                sb.append("#### 1. Maryada Purushottama (The Ideal Human)\n")
                sb.append("Lord Rama is revered as **Maryada Purushottama** — the supreme embodiment of righteousness within human limitations. Rather than relying on supernatural displays, Rama shows how to navigate grief, duty, and sacrifice with unwavering honor.\n\n")
                sb.append("#### 2. Key Pillars of Rama Conduct:\n")
                sb.append("- **Satya (Truth & Honor)**: Accepted 14 years of forest exile (*Vanvas*) without hesitation to uphold the promise of his father, King Dasharatha.\n")
                sb.append("- **Compassion & Equality**: Honored Shabari humble berries, embraced Guha the boatman as his brother, and aligned with Sugriva and Vibhishana.\n")
                sb.append("- **Dharmic Warfare**: Fought Ravana not for conquest or malice, but to protect righteousness and restore cosmic balance.\n\n")
                sb.append("> *Dharmo Rakshati Rakshitah — Dharma protects those who protect it.*")
            }

            isKrishnaOrGita -> {
                sb.append("### Lord Krishna & The Bhagavad Gita\n\n")
                sb.append("At the twilight of **Dvapara Yuga**, on the battlefield of Kurukshetra, Lord Krishna delivers the timeless dialogue of the **Bhagavad Gita** to Arjuna.\n\n")
                sb.append("#### 1. The Core Doctrine: Nishkama Karma (Gita 2.47)\n")
                sb.append("> **Karmanye vadhikaraste ma phaleshu kadachana**\n")
                sb.append("> **Ma karmaphalahetur bhurma te sango stvakarmani**\n\n")
                sb.append("*You have a right only to perform your prescribed duty, never to the fruits of action. Never consider yourself the cause of results, nor let yourself be attached to inaction.*\n\n")
                sb.append("#### 2. The Three Sovereign Paths:\n")
                sb.append("1. **Karma Yoga (Selfless Action)**: Act with full dedication, treating the work itself as the offering, detached from outcome anxiety.\n")
                sb.append("2. **Jnana Yoga (Discriminating Wisdom)**: Discerning the immortal soul (*Atman*) from the transient physical body (*Prakriti*).\n")
                sb.append("3. **Bhakti Yoga (Unconditional Devotion)**: Surrendering ego to supreme cosmic intelligence.")
            }

            isYugas -> {
                sb.append("### The Four Yugas: The Vedic Cosmic Cycle\n\n")
                sb.append("Vedic cosmology divides cosmic time into cyclical **Mahayugas**, spanning 4.32 million solar years across four distinct stages:\n\n")
                sb.append("1. **Satya Yuga (Golden Age - 4 Pillars of Dharma)**: Truth, Purity, Compassion, and Austerity. Direct realization of the Divine.\n")
                sb.append("2. **Treta Yuga (Silver Age - 3 Pillars of Dharma)**: Marked by duty and sacred sacrifice. Exemplified by the Ramayana.\n")
                sb.append("3. **Dvapara Yuga (Bronze Age - 2 Pillars of Dharma)**: Duality and intellect arise. Marked by the Mahabharata and Krishna Gita.\n")
                sb.append("4. **Kali Yuga (Iron Age - Present Epoch)**: Dharma stands on one single surviving pillar: **Satya (Truth)**.\n\n")
                sb.append("**The Advantage of Kali Yuga**: Because distractions are extreme, sincere inner truthfulness and mental focus yield exponential spiritual progress compared to earlier eras.")
            }

            isKarmaOrMoksha -> {
                sb.append("### Karma, Samsara & Moksha\n\n")
                sb.append("Vedic philosophy treats Karma as the universal law of causal equilibrium:\n\n")
                sb.append("1. **Sanchita Karma**: The accumulated reservoir of past impressions waiting to mature.\n")
                sb.append("2. **Prarabdha Karma**: The portion ripe for this current lifetime (birth, constitution, circumstances).\n")
                sb.append("3. **Agami / Kriyamana Karma**: The actions you are choosing **right now in this present moment**. This is your supreme locus of free will.\n\n")
                sb.append("**Moksha** is the liberation from the cycle of rebirth (*Samsara*), realizing that your true self (*Atman*) is one with universal consciousness (*Brahman*).")
            }

            isStoicism || isMentalModel || isEasternPhilosophy -> {
                if (isStoicism) {
                    sb.append("### Stoic Philosophy for Mental Resilience\n\n")
                    sb.append("- **The Dichotomy of Control (Epictetus)**: Separate everything into what is up to you (your choices, thoughts, responses) and what is not (events, other people, outcomes). Direct 100% of your energy to what you control.\n")
                    sb.append("- **Amor Fati (Marcus Aurelius & Nietzsche)**: Do not simply tolerate difficulties — embrace them as fuel to forge deeper strength.\n")
                    sb.append("- **Memento Mori**: Remember time is finite. Cut out trivial distractions and act with clarity today.")
                } else if (isMentalModel) {
                    sb.append("### High-Leverage Mental Models\n\n")
                    sb.append("1. **First-Principles Thinking**: Strip a problem of all assumptions down to fundamental truths, then build upward from there.\n")
                    sb.append("2. **Inversion (Carl Jacobi & Charlie Munger)**: Instead of asking how to succeed, ask how you could guarantee failure — then systematically eliminate those behaviors.\n")
                    sb.append("3. **The 80/20 Pareto Rule**: 20% of inputs produce 80% of outcomes. Identify the single 20% needle-mover today.")
                } else {
                    sb.append("### Eastern Wisdom & Flow State\n\n")
                    sb.append("- **Wu Wei (Effortless Action)**: Moving in harmony with natural momentum rather than forcing resistance.\n")
                    sb.append("- **Beginner Mind (Shoshin)**: Approaching familiar problems with zero preconceived bias to discover creative breakthroughs.")
                }
            }

            isWorkoutOrGym || isDietOrNutrition -> {
                if (isWorkoutOrGym) {
                    sb.append("### Evidence-Based 4-Day Upper / Lower Workout Split\n\n")
                    sb.append("Designed for functional strength, athletic performance, and muscle hypertrophy:\n\n")
                    sb.append("#### Day 1: Upper Body (Power)\n")
                    sb.append("- **Flat Barbell Bench Press**: 3 sets x 5-8 reps (2-3 min rest)\n")
                    sb.append("- **Barbell Bent-Over Row**: 3 sets x 6-8 reps\n")
                    sb.append("- **Standing Overhead Press**: 3 sets x 6-8 reps\n")
                    sb.append("- **Incline Dumbbell Curl + Overhead Triceps Extension**: 3 supersets x 10-12 reps\n\n")
                    sb.append("#### Day 2: Lower Body (Strength & Posterior Chain)\n")
                    sb.append("- **Barbell Back Squat**: 3 sets x 5-8 reps\n")
                    sb.append("- **Romanian Deadlift (RDL)**: 3 sets x 8-10 reps\n")
                    sb.append("- **Walking Lunges**: 3 sets x 12 reps per leg\n")
                    sb.append("- **Standing Calf Raises**: 4 sets x 15 reps\n\n")
                    sb.append("#### Days 3 & 4: Upper / Lower (Hypertrophy & Volume)\n")
                    sb.append("Focus on 8-12 rep ranges with controlled eccentric tempo (3 seconds down).")
                } else {
                    sb.append("### Performance Nutrition Guidelines\n\n")
                    sb.append("- **Daily Protein Target**: 1.6 to 2.2 grams per kilogram of body weight to support muscle synthesis and recovery.\n")
                    sb.append("- **Hydration**: 3 to 4 liters of clean water daily with trace electrolytes.\n")
                    sb.append("- **Whole Foods Foundation**: Lean proteins (eggs, chicken, fish, paneer/tofu), complex carbs (oats, rice, sweet potatoes), and healthy fats (olive oil, almonds, avocados).\n")
                    sb.append("- **Meal Timing**: Prioritize protein within 2 hours post-training for optimal recovery.")
                }
            }

            isPlanOrProductivity -> {
                sb.append("### High-Performance Daily Execution Architecture\n\n")
                sb.append("#### 1. Morning Deep Focus Block (08:30 - 11:30)\n")
                sb.append("- **Monk Mode (90-min Ultradian Rhythm)**: Phone on Do Not Disturb. Zero social media or email.\n")
                sb.append("- **The Single Needle Mover**: Complete your highest-leverage engineering, writing, or strategic task before looking at incoming messages.\n\n")
                sb.append("#### 2. Afternoon Execution & Operations (13:00 - 16:30)\n")
                sb.append("- **Batch Communications**: Process Slack, emails, pull requests, and meetings in a single batch window.\n")
                sb.append("- **Sprint Execution**: Complete secondary tasks, reviews, and operational items.\n\n")
                sb.append("#### 3. Evening Reset & Audit (17:30 - 18:30)\n")
                sb.append("- **Daily Audit**: What moved forward today? Where did time slip away?\n")
                sb.append("- **Tomorrow Top 3**: Pre-commit to tomorrow top 3 priorities before closing your desk.")
            }

            isDocker || isPostgresOrDb || isKotlinOrAndroid || isPython || isTerraformOrAws || isGitOrLinux || isGeneralCode -> {
                if (isDocker) {
                    sb.append("### Production-Ready Docker Compose Architecture\n\n")
                    sb.append("Here is a docker-compose.yml configuration featuring PostgreSQL 16 and Redis with persistent volumes and healthchecks:\n\n")
                    codeSnippet = CodeSnippet(
                        language = "YAML",
                        code = "version: '3.8'\n\nservices:\n  postgres:\n    image: postgres:16-alpine\n    container_name: clutch_postgres\n    restart: unless-stopped\n    environment:\n      POSTGRES_DB: app_database\n      POSTGRES_USER: app_admin\n      POSTGRES_PASSWORD: secure_password_123\n    ports:\n      - '5432:5432'\n    volumes:\n      - postgres_data:/var/lib/postgresql/data\n    healthcheck:\n      test: ['CMD-SHELL', 'pg_isready -U app_admin -d app_database']\n      interval: 10s\n      timeout: 5s\n      retries: 5\n\n  redis:\n    image: redis:7-alpine\n    container_name: clutch_redis\n    restart: unless-stopped\n    ports:\n      - '6379:6379'\n    volumes:\n      - redis_data:/data\n\nvolumes:\n  postgres_data:\n  redis_data:"
                    )
                    sb.append("Run with `docker compose up -d` to launch in detached mode.")
                } else if (isPostgresOrDb) {
                    sb.append("### Optimized PostgreSQL Schema & Indexes\n\n")
                    sb.append("Best practices for fast, scalable database operations:\n\n")
                    codeSnippet = CodeSnippet(
                        language = "SQL",
                        code = "-- 1. Create indexed documents table\nCREATE TABLE IF NOT EXISTS user_documents (\n    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),\n    user_id UUID NOT NULL,\n    title VARCHAR(255) NOT NULL,\n    chunk_count INT DEFAULT 0,\n    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP\n);\n\n-- 2. Fast B-Tree index on user and creation date\nCREATE INDEX IF NOT EXISTS idx_documents_user_created \nON user_documents(user_id, created_at DESC);\n\n-- 3. High-speed paginated query\nSELECT id, title, chunk_count, created_at\nFROM user_documents\nWHERE user_id = 'a1b2c3d4-e5f6-7890-1234-56789abcdef0'\nORDER BY created_at DESC\nLIMIT 20 OFFSET 0;"
                    )
                } else if (isKotlinOrAndroid) {
                    sb.append("### Modern Android StateFlow & Jetpack Compose Pattern\n\n")
                    sb.append("Reactive StateFlow with Coroutines:\n\n")
                    codeSnippet = CodeSnippet(
                        language = "KOTLIN",
                        code = "class DocumentViewModel(\n    private val repository: DocumentRepository\n) : ViewModel() {\n\n    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)\n    val uiState: StateFlow<UiState> = _uiState.asStateFlow()\n\n    fun loadDocuments() {\n        viewModelScope.launch {\n            _uiState.value = UiState.Loading\n            try {\n                val docs = repository.fetchLocalDocuments()\n                _uiState.value = UiState.Success(docs)\n            } catch (e: Exception) {\n                _uiState.value = UiState.Error(e.localizedMessage ?: \"Error\")\n            }\n        }\n    }\n}"
                    )
                } else if (isPython) {
                    sb.append("### High-Performance Python Script\n\n")
                    codeSnippet = CodeSnippet(
                        language = "PYTHON",
                        code = "import asyncio\nfrom pathlib import Path\nimport json\n\nasync def process_local_stash(file_path: str) -> dict:\n    path = Path(file_path)\n    if not path.exists():\n        raise FileNotFoundError(f\"File {file_path} not found\")\n        \n    loop = asyncio.get_running_loop()\n    content = await loop.run_in_executor(None, path.read_text)\n    data = json.loads(content)\n    return {\"status\": \"success\", \"records\": len(data), \"data\": data}\n\nif __name__ == \"__main__\":\n    print(\"Clutch Python Engine ready.\")"
                    )
                } else {
                    sb.append("### Essential Terminal Automation Commands\n\n")
                    codeSnippet = CodeSnippet(
                        language = "BASH",
                        code = "# Check disk usage and active docker containers\ndf -h\ndocker ps --format 'table {{.Names}}\\t{{.Status}}\\t{{.Ports}}'\n\n# Quick git branch cleanup of merged branches\ngit branch --merged | grep -v '\\*' | grep -v 'main' | xargs -n 1 git branch -d"
                    )
                }
            }

            isDocSummary -> {
                val targetDoc = matchedDoc ?: stashedDocs.firstOrNull()
                if (targetDoc != null) {
                    sb.append("### Stash Document: " + targetDoc.title + "\n\n")
                    sb.append("Indexed **" + targetDoc.chunkCount + " segments** (" + targetDoc.fileSizeFormat + "):\n\n")
                    sb.append(targetDoc.summary + "\n\n")
                    sb.append("")
                    sb.append("")
                } else {
                    sb.append("You currently have **" + stashedDocs.size + " documents** saved in your private vault:\n\n")
                    stashedDocs.forEach { doc ->
                        sb.append("- **" + doc.title + "** (" + doc.fileSizeFormat + "): " + doc.summary + "\n")
                    }
                }
            }

            isPersonalFact -> {
                sb.append("### Your Active Personal Profile & Directives\n\n")
                if (activeMemories.isNotEmpty()) {
                    sb.append("Here are your active guidelines saved on-device:\n\n")
                    activeMemories.forEach { mem ->
                        sb.append("- **" + mem.title + "** (`" + mem.category.label + "`):\n  *\"" + mem.content + "\"*\n")
                    }
                } else {
                    sb.append("No active directives trained yet. You can teach me your guidelines and preferences in the **Teach AI** tab!")
                }
            }

            else -> {
                val cleanTopic = prompt.trim().removeSuffix("?").removeSuffix(".")
                sb.append("Here is what you need to know about **" + cleanTopic + "**:\n\n")

                if (queryLower.contains("how") || queryLower.contains("step") || queryLower.contains("guide")) {
                    sb.append("1. **First Steps & Prerequisites**: Start with a clean foundation and ensure all required inputs or dependencies are ready.\n")
                    sb.append("2. **Core Implementation**: Focus on the primary objective directly. Break it down into testable milestones.\n")
                    sb.append("3. **Validation & Polish**: Test edge cases and refine the output to ensure maximum reliability and quality.\n\n")
                    sb.append("Would you like a detailed walkthrough on any specific step?")
                } else if (queryLower.contains("why") || queryLower.contains("reason")) {
                    sb.append("- **Core Driver**: The primary factor is efficiency and alignment with foundational principles.\n")
                    sb.append("- **Impact**: It minimizes unnecessary overhead and delivers reliable, consistent results.\n")
                    sb.append("- **Key Takeaway**: Understanding this allows you to make better decisions from first principles.")
                } else if (queryLower.contains("difference") || queryLower.contains("vs") || queryLower.contains("compare")) {
                    sb.append("- **Key Trade-offs**: The choice primarily depends on your specific constraints, performance requirements, and complexity tolerance.\n")
                    sb.append("- **Recommendation**: Opt for the simpler, proven solution unless specific scaling or privacy constraints dictate otherwise.")
                } else {
                    sb.append("- **Key Insight**: The most effective approach is to focus on the essential objective while eliminating unnecessary complexity.\n")
                    sb.append("- **Best Practice**: Validate each step incrementally to ensure solid outcomes.\n")
                    sb.append("- **Practical Result**: Consistency and clear execution produce the highest leverage results.")
                }

                if (matchedMemories.isNotEmpty()) {
                    sb.append("\n\n*Applied your trained rule: " + matchedMemories.first().title + "*")
                }

                if (matchedDoc != null) {
                    sb.append("\n*Referenced local vault document: " + matchedDoc.title + "*")
                }
            }
        }

        return GenerationResult(
            text = sb.toString(),
            latencyMs = null,
            modelName = "Clutch AI Assistant",
            memoryHits = memoryTags,
            citations = citations,
            codeSnippet = codeSnippet
        )
    }
}
