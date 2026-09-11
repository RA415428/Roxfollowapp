const fs = require('fs');
let code = fs.readFileSync('src/utils/authService.ts', 'utf8');

// Ensure native Google Sign-in import
if (!code.includes('@capacitor-firebase/authentication')) {
  code = `import { FirebaseAuthentication } from '@capacitor-firebase/authentication';\n` + code;
}

fs.writeFileSync('src/utils/authService.ts', code);
console.log('authService updated');
