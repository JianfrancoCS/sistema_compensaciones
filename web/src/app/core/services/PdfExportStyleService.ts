import { Injectable } from '@angular/core';
import { VariableSelectOption } from '@shared/types/variable';

import pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import htmlToPdfmake from 'html-to-pdfmake';

(pdfMake as any).vfs = pdfFonts.vfs;

pdfMake.fonts = {
  Roboto: {
    normal: 'Roboto-Regular.ttf',
    bold: 'Roboto-Medium.ttf',
    italics: 'Roboto-Italic.ttf',
    bolditalics: 'Roboto-MediumItalic.ttf'
  },
};

@Injectable({
  providedIn: 'root'
})
export class PdfExportStyleService {
  async exportQuillContentHtml(htmlContent: string, variables: VariableSelectOption[], fileName = 'documento.pdf'): Promise<string> {
    let processedHtmlContent = htmlContent;

    variables.forEach(variable => {
      const regex = new RegExp(`\\{\\{${variable.code}\\}\\}`, 'g');
      if (variable.defaultValue) {
        processedHtmlContent = processedHtmlContent.replace(regex, variable.defaultValue);
      }
    });

    processedHtmlContent = processedHtmlContent.replace(/\{\{.*?\}\}/g, '_____________');
    processedHtmlContent = processedHtmlContent.replace(/style="([^"]*)"/g, (match, styleContent) => {
      const cleanedStyle = styleContent.replace(/font-family:[^;]*;?/g, '').trim();
      return cleanedStyle ? `style="${cleanedStyle}"` : '';
    });

    const tempElement = document.createElement('div');
    tempElement.innerHTML = processedHtmlContent;
    const content = htmlToPdfmake(tempElement.innerHTML);

    const docDefinition = {
      content: content,
      defaultStyle: {
        font: 'Roboto',
      },
      pageMargins: [85, 71, 85, 71] as [number, number, number, number]
    };

    return new Promise((resolve, reject) => {
      pdfMake.createPdf(docDefinition).getBlob((blob) => {
        const blobUrl = URL.createObjectURL(blob);
        resolve(blobUrl);
      });
    });
  }
}
