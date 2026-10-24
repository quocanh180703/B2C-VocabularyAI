const fs = require('fs');
const path = require('path');

const dataDir = 'd:/demo-datn/vocabulary-ai-backend/src/main/resources/db/data_quiz';

function parseQuizFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf-8').trim();
  const objects = [];
  let depth = 0, inString = false, escape = false, startIdx = -1;
  for (let i = 0; i < content.length; i++) {
    const ch = content[i];
    if (escape) { escape = false; continue; }
    if (ch === '\\') { escape = true; continue; }
    if (ch === '"') { inString = !inString; continue; }
    if (inString) continue;
    if (ch === '{') { if (depth === 0) startIdx = i; depth++; }
    else if (ch === '}') {
      depth--;
      if (depth === 0 && startIdx !== -1) {
        objects.push(JSON.parse(content.substring(startIdx, i + 1)));
        startIdx = -1;
      }
    }
  }
  return objects;
}

const wordDistractorMap = {
  "'d": ["'ll", "'ve", "'s"],
  "'m": ["'re", "'s", "'ve"],
  "'ve": ["'d", "'s", "'ll"],
  "i'll": ["I'd", "I'm", "I've"],
  "we'll": ["we'd", "we're", "we've"],
  "other": ["another", "others", "the other"],
  "another": ["other", "others", "each other"],
  "others": ["other", "another", "the others"],
  "same": ["similar", "like", "alike"],
  "next": ["near", "close", "adjacent"],
  "together": ["alone", "apart", "each other"],
  "there": ["it", "here", "they"],
  "amount": ["number", "quantity", "deal"],
  "deal": ["amount", "number", "quantity"],
  "time": ["moment", "period", "while"],
  "two": ["both", "pair", "double"],
  "large": ["great", "big", "huge"],
  "tall": ["high", "long", "big"],
  "long": ["far", "wide", "deep"],
  "longer": ["more", "further", "farther"],
  "ago": ["before", "past", "since"],
  "how": ["what", "which", "where"],
  "not": ["no", "never", "none"],
  "also": ["as well", "too", "either"],
  "soon": ["early", "fast", "quickly"],
  "forward": ["ahead", "onward", "forth"],
  "far": ["near", "away", "distant"],
  "well": ["good", "better", "best"],
  "later": ["afterwards", "sooner", "then"],
  "afterwards": ["later", "then", "after"],
  "only": ["just", "merely", "alone"],
  "first": ["initial", "primary", "one"],
  "away": ["back", "out", "off"],
  "back": ["away", "out", "again"],
  "home": ["house", "place", "room"],
  "way": ["means", "method", "road"],
  "otherwise": ["therefore", "instead", "however"],
  "unlike": ["different", "opposite", "dislike"],
  "now": ["then", "currently", "present"],
  "given": ["provided", "considering", "assuming"],
  "considering": ["provided", "regarding", "supposing"],
  "seeing": ["knowing", "since", "given"],
  "even": ["also", "still", "just"],
  "above": ["over", "up", "top"],
  "good": ["well", "fine", "nice"],
  "great": ["big", "large", "huge"],
  "best": ["better", "good", "most"],
  "worst": ["worse", "bad", "least"],
  "often": ["always", "sometimes", "rarely"],
  "once": ["twice", "one", "ever"],
  "whenever": ["wherever", "whatever", "whoever"],
  "wherever": ["whenever", "whatever", "everywhere"],
  "body": ["person", "one", "head"],
  "cases": ["times", "points", "events"],
  "majority": ["minority", "most", "amount"],
  "made": ["done", "took", "had"],
  "hope": ["wish", "expect", "want"],
  "watch": ["look", "see", "view"],
  "bought": ["brought", "paid", "got"],
  "become": ["turn", "get", "grow"],
  "will become": ["becomes", "became", "is becoming"],
  "learn": ["study", "know", "teach"],
  "got": ["had", "received", "took"],
  "started": ["began", "opened", "went"],
  "pushed": ["pulled", "pressed", "forced"],
  "turned": ["moved", "became", "changed"],
  "bring": ["take", "fetch", "carry"],
  "walked": ["went", "ran", "stepped"],
  "enjoyed": ["liked", "loved", "preferred"],
  "prefer": ["like", "favour", "choose"],
  "stand": ["sit", "stay", "bear"],
  "lacking": ["missing", "needing", "wanting"],
  "going": ["coming", "heading", "moving"],
  "makes": ["does", "takes", "gives"],
  "making": ["doing", "taking", "giving"],
  "stay": ["leave", "remain", "wait"],
  "face": ["meet", "confront", "encounter"],
  "cause": ["make", "lead", "result"],
  "goes": ["comes", "moves", "runs"],
  "use": ["make", "take", "apply"],
  "speaking": ["talking", "saying", "telling"],
  "think": ["believe", "consider", "know"],
  "accomplish": ["achieve", "complete", "finish"],
  "comes": ["goes", "arrives", "reaches"],
  "taking": ["making", "having", "getting"],
  "carry": ["bring", "take", "hold"],
  "need": ["require", "demand", "must"]
};

const distractorGroups = {
  prepositions: ['in', 'on', 'at', 'to', 'for', 'with', 'by', 'from', 'of', 'about', 'into', 'through', 'under', 'over', 'between', 'among', 'behind', 'across', 'against', 'towards', 'upon', 'without', 'within', 'beyond', 'throughout', 'along', 'around', 'round', 'near', 'past', 'down', 'up', 'off', 'out', 'outside'],
  time_prepositions: ['since', 'for', 'during', 'until', 'till', 'before', 'after', 'by', 'while', 'when'],
  articles: ['a', 'an', 'the', 'some', 'any', 'one', 'no'],
  demonstratives: ['this', 'that', 'these', 'those'],
  pronouns_subj: ['I', 'you', 'he', 'she', 'it', 'we', 'they'],
  pronouns_obj: ['me', 'you', 'him', 'her', 'it', 'us', 'them'],
  possessive_adj: ['my', 'your', 'his', 'her', 'its', 'our', 'their'],
  possessive_pron: ['mine', 'yours', 'his', 'hers', 'ours', 'theirs'],
  reflexive: ['myself', 'yourself', 'himself', 'herself', 'itself', 'ourselves', 'themselves'],
  relative: ['who', 'whom', 'whose', 'which', 'that', 'where', 'when', 'why', 'what'],
  conjunctions_contrast: ['although', 'though', 'even though', 'while', 'whilst', 'whereas', 'however', 'despite', 'but', 'yet'],
  conjunctions_reason: ['because', 'since', 'as', 'for', 'so'],
  conjunctions_condition: ['if', 'whether', 'unless', 'provided', 'providing', 'supposing', 'assuming'],
  conjunctions_coordinating: ['and', 'but', 'or', 'so', 'yet', 'nor'],
  quantifiers: ['all', 'both', 'each', 'every', 'either', 'neither', 'none', 'any', 'some', 'no', 'whole'],
  quantity_degree: ['much', 'many', 'more', 'most', 'less', 'least', 'few', 'little', 'fewer'],
  degree_adverbs: ['very', 'too', 'so', 'such', 'quite', 'rather', 'enough', 'fairly', 'extremely'],
  negative_adverbs: ['hardly', 'scarcely', 'barely', 'rarely', 'seldom', 'never'],
  aux_be: ['am', 'is', 'are', 'was', 'were', 'been', 'being', 'be'],
  aux_have: ['have', 'has', 'had', 'having'],
  aux_do: ['do', 'does', 'did', 'doing', 'done'],
  modals: ['can', 'could', 'will', 'would', 'shall', 'should', 'may', 'might', 'must', 'ought'],
  indefinite_people: ['someone', 'anyone', 'everyone', 'no one', 'somebody', 'anybody', 'everybody', 'nobody'],
  indefinite_things: ['something', 'anything', 'everything', 'nothing'],
  indefinite_places: ['somewhere', 'anywhere', 'everywhere', 'nowhere'],
  comparisons: ['than', 'as', 'like', 'so'],
  collocation_idioms: {
    'spite': ['despite', 'stead', 'view'],
    'fact': ['case', 'truth', 'point'],
    'order': ['view', 'case', 'aim'],
    'case': ['event', 'fact', 'time'],
    'result': ['consequence', 'reason', 'effect'],
    'consequence': ['result', 'outcome', 'effect'],
    'terms': ['respect', 'regard', 'relation'],
    'mind': ['head', 'heart', 'memory'],
    'doubt': ['question', 'wonder', 'worry'],
    'matter': ['problem', 'trouble', 'issue'],
    'means': ['way', 'manner', 'method'],
    'question': ['doubt', 'problem', 'matter'],
    'degree': ['extent', 'level', 'measure'],
    'extent': ['degree', 'measure', 'limit'],
    'place': ['part', 'room', 'space'],
    'course': ['sure', 'certain', 'way'],
    'front': ['ahead', 'face', 'forward'],
    'reason': ['cause', 'purpose', 'motive'],
    'able': ['capable', 'possible', 'likely'],
    'capable': ['able', 'competent', 'fitted'],
    'used': ['accustomed', 'familiar', 'known'],
    'likely': ['probable', 'possible', 'sure'],
    'worth': ['valuable', 'worthy', 'costly'],
    'according': ['due', 'owing', 'referring'],
    'owing': ['due', 'because', 'thanks'],
    'due': ['owing', 'because', 'thanks'],
    'thanks': ['due', 'owing', 'regards'],
    'apart': ['except', 'besides', 'away'],
    'except': ['besides', 'apart', 'without'],
    'besides': ['except', 'apart', 'furthermore'],
    'instead': ['in spite', 'regardless', 'contrary'],
    'regardless': ['irrespective', 'despite', 'careless'],
    'irrespective': ['regardless', 'despite', 'independent'],
    'vain': ['waste', 'loss', 'pointless'],
    'take': ['make', 'have', 'get'],
    'make': ['do', 'take', 'have'],
    'give': ['bring', 'offer', 'provide'],
    'get': ['take', 'have', 'receive'],
    'look': ['see', 'watch', 'stare'],
    'turn': ['come', 'go', 'change'],
    'keep': ['hold', 'stay', 'remain'],
    'put': ['set', 'place', 'lay'],
    'come': ['go', 'arrive', 'reach'],
    'go': ['come', 'leave', 'move']
  }
};

function getDistractors(targetWord, acceptedAnswers) {
  const isCap = targetWord[0] === targetWord[0].toUpperCase() && targetWord[0] !== targetWord[0].toLowerCase();
  const lower = targetWord.toLowerCase().trim();
  const acceptedLower = acceptedAnswers.map(a => a.toLowerCase().trim());

  let candidates = [];

  if (wordDistractorMap[lower]) {
    candidates = wordDistractorMap[lower];
  } else if (distractorGroups.collocation_idioms[lower]) {
    candidates = distractorGroups.collocation_idioms[lower];
  } else {
    for (const [groupName, words] of Object.entries(distractorGroups)) {
      if (Array.isArray(words) && words.includes(lower)) {
        candidates = words.filter(w => !acceptedLower.includes(w.toLowerCase()));
        break;
      }
    }
  }

  let filtered = candidates.filter(w => !acceptedLower.includes(w.toLowerCase()));

  if (filtered.length < 3) {
    const fallback = ['in', 'at', 'on', 'to', 'for', 'with', 'by', 'as', 'that', 'so'];
    for (const fb of fallback) {
      if (!acceptedLower.includes(fb) && !filtered.includes(fb) && fb !== lower) {
        filtered.push(fb);
      }
      if (filtered.length >= 3) break;
    }
  }

  return filtered.slice(0, 3).map(w => {
    if (isCap) {
      return w.charAt(0).toUpperCase() + w.slice(1);
    }
    return w;
  });
}

function generateExplanation(targetWord, sentence) {
  const lower = targetWord.toLowerCase().trim();
  if (['in', 'at', 'on', 'to', 'for', 'with', 'by', 'from', 'of', 'about', 'into', 'under', 'over', 'between', 'among'].includes(lower)) {
    return `Sử dụng giới từ '${targetWord}' là chính xác nhất theo quy tắc ngữ pháp hoặc ngữ cảnh/cụm từ đi kèm trong câu.`;
  }
  if (['who', 'whom', 'whose', 'which', 'that', 'where', 'when', 'why'].includes(lower)) {
    return `Đại từ quan hệ '${targetWord}' được dùng để liên kết mệnh đề và thay thế cho danh từ đứng trước.`;
  }
  if (['although', 'though', 'while', 'whereas', 'however', 'despite', 'because', 'since', 'as', 'so', 'if', 'unless', 'whether'].includes(lower)) {
    return `Sử dụng liên từ '${targetWord}' phù hợp với cấu trúc ngữ pháp và ý nghĩa kết nối giữa hai mệnh đề.`;
  }
  if (['a', 'an', 'the', 'some', 'any', 'every', 'each', 'all', 'both'].includes(lower)) {
    return `Dùng từ xác định/mạo từ '${targetWord}' phù hợp với danh từ đếm được/không đếm được theo sau.`;
  }
  if (['much', 'many', 'more', 'most', 'less', 'least', 'few', 'little'].includes(lower)) {
    return `'${targetWord}' là lượng từ phù hợp với sắc thái định lượng hoặc cấu trúc so sánh trong câu.`;
  }
  if (['me', 'him', 'her', 'us', 'them', 'my', 'your', 'his', 'their', 'myself', 'himself'].includes(lower)) {
    return `Đại từ '${targetWord}' giữ đúng vai trò ngữ pháp (tân ngữ/sở hữu/phản thân) trong câu.`;
  }
  return `Đáp án '${targetWord}' là từ phù hợp nhất về mặt ngữ pháp và kết hợp từ (collocation) trong ngữ cảnh này.`;
}

function transformExercise(ex, level) {
  let gapIdx = 0;
  const annotatedPassage = ex.text.replace(/_/g, () => {
    gapIdx++;
    return `[${gapIdx}]`;
  });

  const description = `Read the following text and choose the correct answer for each gap [1] - [${ex.gaps.length}]:\n\n${annotatedPassage}`;

  const questions = ex.gaps.map((gap, idx) => {
    const targetGapNum = idx + 1;
    let curGap = 0;
    const resolvedText = ex.text.replace(/_/g, () => {
      curGap++;
      if (curGap === targetGapNum) return `_____`;
      const other = ex.gaps[curGap - 1];
      return other ? other.answers[0] : '...';
    });

    // Normalize text and protect decimals / abbreviations
    const normalized = resolvedText.replace(/\r/g, '').replace(/\n+/g, ' ');
    const protectedText = normalized
      .replace(/(\d)\.(\d)/g, '$1__DOT__$2')
      .replace(/\bp\.m\./gi, 'p__DOT__m__DOT__')
      .replace(/\ba\.m\./gi, 'a__DOT__m__DOT__')
      .replace(/\bMr\./gi, 'Mr__DOT__')
      .replace(/\bMrs\./gi, 'Mrs__DOT__')
      .replace(/\bMs\./gi, 'Ms__DOT__')
      .replace(/\bDr\./gi, 'Dr__DOT__')
      .replace(/\be\.g\./gi, 'e__DOT__g__DOT__')
      .replace(/\bi\.e\./gi, 'i__DOT__e__DOT__');

    const sentences = protectedText.match(/[^.?!]+[.?!]+|[^.?!]+$/g) || [protectedText];
    let matchedSentence = '';
    for (const sent of sentences) {
      if (sent.includes('_____')) {
        matchedSentence = sent.replace(/__DOT__/g, '.').replace(/\s+/g, ' ').trim();
        break;
      }
    }

    if (!matchedSentence) {
      matchedSentence = `[${targetGapNum}] Choose the correct word for gap ${targetGapNum}: _____`;
    } else {
      matchedSentence = `[${targetGapNum}] ${matchedSentence}`;
    }

    const primaryAnswer = gap.answers[0];
    const distractors = getDistractors(primaryAnswer, gap.answers);
    
    const allOptions = [primaryAnswer, ...distractors];
    // Deterministic shuffle
    const correctIdx = (targetGapNum + primaryAnswer.length) % 4;
    const temp = allOptions[0];
    allOptions[0] = allOptions[correctIdx];
    allOptions[correctIdx] = temp;

    const explanation = generateExplanation(primaryAnswer, matchedSentence);

    return {
      gapNumber: targetGapNum,
      content: matchedSentence,
      options: allOptions,
      correctAnswer: correctIdx,
      answers: gap.answers,
      explanation: explanation
    };
  });

  return {
    id: ex.id,
    title: ex.title,
    level: level,
    timeLimit: Math.max(300, ex.gaps.length * 60),
    description: description,
    passage: ex.text,
    questions: questions
  };
}

function escapeSql(str) {
  if (str === null || str === undefined) return 'NULL';
  return "'" + str.replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n').replace(/\r/g, '') + "'";
}

const levels = ['A1', 'A2', 'B1', 'B2', 'C1'];
const allQuizzes = [];
let totalQuestionsCount = 0;

levels.forEach(level => {
  const p = path.join(dataDir, `${level}.json`);
  const rawList = parseQuizFile(p);
  const transformed = rawList.map(ex => transformExercise(ex, level));
  allQuizzes.push(...transformed);

  const outJson = path.join(dataDir, `${level}_mcq.json`);
  fs.writeFileSync(outJson, JSON.stringify(transformed, null, 2), 'utf-8');
  
  let qCount = 0;
  transformed.forEach(q => qCount += q.questions.length);
  totalQuestionsCount += qCount;
  console.log(`[${level}] Saved ${transformed.length} quizzes (${qCount} questions) to ${level}_mcq.json`);
});

// Save all consolidated
const consolidatedFile = path.join(dataDir, 'all_quizzes_mcq.json');
fs.writeFileSync(consolidatedFile, JSON.stringify(allQuizzes, null, 2), 'utf-8');
console.log(`Saved all ${allQuizzes.length} quizzes (${totalQuestionsCount} questions) to all_quizzes_mcq.json`);

// Generate SQL migration file
const sqlLines = [
  '-- Migration: Import 133 Open Cloze & MCQ Quizzes (A1 - C1)',
  '-- Generated automatically',
  'START TRANSACTION;',
  ''
];

allQuizzes.forEach((q, qIndex) => {
  const quizVar = `@quiz_id_${qIndex}`;
  sqlLines.push(`-- Quiz ${qIndex + 1}: [${q.level}] ${q.title.replace(/'/g, '')}`);
  sqlLines.push(`INSERT INTO quizzes (title, description, level, time_limit, status, created_at, updated_at) VALUES (` +
    `${escapeSql(q.title)}, ` +
    `${escapeSql(q.description)}, ` +
    `${escapeSql(q.level)}, ` +
    `${q.timeLimit}, ` +
    `'PUBLISHED', ` +
    `NOW(), NOW());`
  );
  sqlLines.push(`SET ${quizVar} = LAST_INSERT_ID();`);
  
  q.questions.forEach(quest => {
    sqlLines.push(`INSERT INTO questions (quiz_id, content, options, correct_answer, explanation, created_at) VALUES (` +
      `${quizVar}, ` +
      `${escapeSql(quest.content)}, ` +
      `${escapeSql(JSON.stringify(quest.options))}, ` +
      `${quest.correctAnswer}, ` +
      `${escapeSql(quest.explanation)}, ` +
      `NOW());`
    );
  });
  sqlLines.push('');
});

sqlLines.push('COMMIT;');

const sqlFile = path.join(dataDir, 'import_quizzes.sql');
fs.writeFileSync(sqlFile, sqlLines.join('\n'), 'utf-8');
console.log(`Saved SQL migration to ${sqlFile} (${sqlLines.length} lines)`);
