// Deploy as Web app: Execute as Me; access Anyone. Use ONLY a sheet with public offer data.
// Set SHEET_ID to the Google Sheet ID and TAB to the worksheet name.
const SHEET_ID = '1s9bsZXwuNBbFadj5A6xkvCVgvQIfM93ZRdsX8RapFkE';
const TAB = 'Goods';
function doGet() {
  const sheet = SpreadsheetApp.openById(SHEET_ID).getSheetByName(TAB);
  if (!sheet) throw new Error('Sheet tab not found: ' + TAB);
  const rows = sheet.getDataRange().getValues();
  const columns = rows.shift().map(String);
  const result = rows.map(row => Object.fromEntries(columns.map((key,i)=>[key,row[i]])))
    .filter(row => row.Aktiv === true || String(row.Aktiv).toLowerCase() === 'true')
    .map(row => ({Produkt:String(row.Produkt||''),Beskrivelse:String(row.Beskrivelse||''),Pris:Number(row.Pris)||0,BildeUrl:String(row.BildeUrl||''),Gruppe:String(row.Gruppe||''),Aktiv:true}));
  return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
}
