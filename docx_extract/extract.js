const mammoth = require('mammoth');
const fs = require('fs');

const filePath = '/home/runner/.openclaw/media/inbound/الدستور---7db75496-ce0b-46e8-92e8-23a21a1bc734.docx';
const outputPath = 'constitution.txt';

mammoth.extractRawText({path: filePath})
    .then(function(result){
        fs.writeFileSync(outputPath, result.value);
        console.log('Extraction successful! Content saved to ' + outputPath);
    })
    .catch(function(err){
        console.error('Error extracting text:', err);
        process.exit(1);
    });
