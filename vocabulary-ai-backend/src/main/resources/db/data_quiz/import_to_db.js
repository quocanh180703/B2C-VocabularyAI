const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const sqlPath = path.join(__dirname, 'import_quizzes.sql');
const mysqlBin = 'C:\\Program Files\\MySQL\\MySQL Server 9.3\\bin\\mysql.exe';

console.log('=== Importing 133 Quizzes (1,161 Questions) into datn_2 Database ===');
console.log('SQL File:', sqlPath);

if (!fs.existsSync(sqlPath)) {
  console.error('Error: import_quizzes.sql not found! Please run convert_and_export.js first.');
  process.exit(1);
}

try {
  const cmd = `"${mysqlBin}" -u root -p180703 --default-character-set=utf8mb4 datn_2 < "${sqlPath}"`;
  console.log('Executing MySQL import...');
  execSync(cmd, { stdio: 'inherit', shell: 'cmd.exe' });
  console.log('\n[SUCCESS] Successfully imported all quizzes and questions into datn_2 database!');
} catch (err) {
  console.error('[ERROR] Failed to import via mysql.exe:', err.message);
  process.exit(1);
}
