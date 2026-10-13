const mysql = require('mysql2/promise');

async function getOTP() {
    const connection = await mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: '180703',
        database: 'datn_2'
    });

    const [rows] = await connection.execute(
        'SELECT otp_code, email, otp_type FROM otp_verifications WHERE email LIKE ? AND otp_type = ? ORDER BY created_at DESC LIMIT 1',
        ['testuser%', 'REGISTER']
    );

    if (rows.length > 0) {
        console.log('OTP Code:', rows[0].otp_code);
        console.log('Email:', rows[0].email);
    } else {
        console.log('No OTP found');
    }

    await connection.end();
}

getOTP().catch(console.error);
