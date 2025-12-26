import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, AfterViewInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { VariableSelectOption } from '@shared/types/variable';

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule],
  templateUrl: './editor.html',
  styleUrls: ['./editor.css']
})
export class EditorComponent implements AfterViewInit, OnDestroy, OnChanges {
  @Input() initialContent: string = ' ';
  @Input() availableVariables: VariableSelectOption[] = [];
  @Output() contentChange = new EventEmitter<string>();
  @Output() usedVariablesChange = new EventEmitter<VariableSelectOption[]>();

  @ViewChild('editorContent') editorContentRef!: ElementRef<HTMLDivElement>;

  private mutationObserver!: MutationObserver;
  private draggedChip: HTMLElement | null = null;
  private lastRange: Range | null = null;
  private isContentLoaded = false;

  fontSizeOptions = [
    { label: '12', value: '3' },
    { label: '14', value: '4' },
    { label: '18', value: '5' },
    { label: '24', value: '6' }
  ];
  selectedFontSize: string = '3';

  alignmentOptions = [
    { icon: 'pi pi-align-left', value: 'left' },
    { icon: 'pi pi-align-center', value: 'center' },
    { icon: 'pi pi-align-right', value: 'right' },
    { icon: 'pi pi-align-justify', value: 'justify' }
  ];
  selectedAlignment: string = 'left';

  ngAfterViewInit(): void {
    this.setupMutationObserver();
    this.setupDragDropListeners();
    this.attemptToLoadContent();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialContent'] || changes['availableVariables']) {
      this.attemptToLoadContent();
    }
  }

  ngOnDestroy(): void {
    if (this.mutationObserver) {
      this.mutationObserver.disconnect();
    }
  }

  private attemptToLoadContent(): void {
    if (this.initialContent && this.availableVariables.length > 0 && !this.isContentLoaded && this.editorContentRef) {
      this.isContentLoaded = true;
      this.editorContentRef.nativeElement.innerHTML = this.initialContent;
      setTimeout(() => this.recreateChipsFromInitialContent(), 50);
    }
  }

  private recreateChipsFromInitialContent(): void {
    const editor = this.editorContentRef.nativeElement;
    const walker = document.createTreeWalker(editor, NodeFilter.SHOW_TEXT);
    const textNodes: Text[] = [];
    let node;
    while (node = walker.nextNode()) {
      textNodes.push(node as Text);
    }

    textNodes.forEach(textNode => {
      const text = textNode.textContent || '';
      if (!text.includes('{{')) return;

      const parent = textNode.parentNode;
      if (!parent) return;

      const fragment = document.createDocumentFragment();
      const parts = text.split(/(\{\{[a-zA-Z0-9_]+\}\})/g);

      parts.forEach(part => {
        const match = part.match(/^\{\{([a-zA-Z0-9_]+)\}\}$/);
        if (match) {
          const variableCode = match[1];
          const variable = this.availableVariables.find(v => v.code === variableCode);
          if (variable) {
            const chip = this.createVariableChip(variable);
            fragment.appendChild(chip);
            fragment.appendChild(document.createTextNode('\u00A0'));
          } else {
            fragment.appendChild(document.createTextNode(part));
          }
        } else if (part) {
          fragment.appendChild(document.createTextNode(part));
        }
      });
      parent.replaceChild(fragment, textNode);
    });
    this.updateUsedVariables();
  }

  private setupDragDropListeners(): void {
    const editorElement = this.editorContentRef.nativeElement;
    editorElement.addEventListener('dragstart', this.onDragStart.bind(this));
    editorElement.addEventListener('dragend', this.onDragEnd.bind(this));
    editorElement.addEventListener('dragover', this.onDragOver.bind(this));
    editorElement.addEventListener('dragleave', this.onDragLeave.bind(this));
    editorElement.addEventListener('drop', this.onDrop.bind(this));
  }

  onDragStart(event: DragEvent): void {
    const target = event.target as HTMLElement;
    if (target.classList.contains('variable-chip')) {
      this.draggedChip = target;
      if (event.dataTransfer) {
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/plain', target.dataset['variableCode'] || '');
        event.dataTransfer.setData('text/x-editor-chip', 'true');
        target.style.opacity = '0.5';
      }
    }
  }

  onDragEnd(event: DragEvent): void {
    if (this.draggedChip) {
      this.draggedChip.style.opacity = '1';
    }
    this.draggedChip = null;
    this.editorContentRef.nativeElement.classList.remove('drag-over-active');
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy';
    }
    this.editorContentRef.nativeElement.classList.add('drag-over-active');
    if (document.caretRangeFromPoint) {
      this.lastRange = document.caretRangeFromPoint(event.clientX, event.clientY);
    } else {
      const selection = window.getSelection();
      if (selection && selection.rangeCount > 0) {
        this.lastRange = selection.getRangeAt(0);
      }
    }
  }

  onDragLeave(event: DragEvent): void {
    this.editorContentRef.nativeElement.classList.remove('drag-over-active');
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.editorContentRef.nativeElement.classList.remove('drag-over-active');

    const variableCode = event.dataTransfer?.getData('text/plain');
    if (!variableCode) return;

    const isInternalDrag = !!this.draggedChip;
    let chipToInsert: HTMLElement;

    if (isInternalDrag && this.draggedChip) {
      chipToInsert = this.draggedChip;
    } else {
      const droppedVariable = this.availableVariables.find(v => v.code === variableCode);
      if (!droppedVariable) return;
      chipToInsert = this.createVariableChip(droppedVariable);
    }

    const range = this.lastRange;
    if (range) {
      if (isInternalDrag) {
        this.draggedChip?.remove();
      }
      range.deleteContents();

      const spaceNode = document.createTextNode('\u00A0');
      range.insertNode(spaceNode);
      range.insertNode(chipToInsert);

      const selection = window.getSelection();
      if (selection) {
        const newRange = document.createRange();
        newRange.setStartAfter(spaceNode);
        newRange.collapse(true);
        selection.removeAllRanges();
        selection.addRange(newRange);
      }
    } else {
      if (isInternalDrag) {
        this.draggedChip?.remove();
      }
      this.editorContentRef.nativeElement.appendChild(chipToInsert);
      const spaceNode = document.createTextNode('\u00A0');
      this.editorContentRef.nativeElement.appendChild(spaceNode);
    }

    this.draggedChip = null;
    this.emitContentChange();
    this.updateUsedVariables();
  }

  private createVariableChip(variable: VariableSelectOption): HTMLElement {
    const chip = document.createElement('span');
    chip.contentEditable = 'false';
    chip.className = 'variable-chip';
    chip.dataset['variableCode'] = variable.code;
    chip.draggable = true;

    const codeSpan = document.createElement('span');
    codeSpan.textContent = variable.code;

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.innerHTML = '×';
    removeBtn.className = 'remove-chip-btn';

    removeBtn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      this.removeVariableChip(chip);
    });

    chip.appendChild(codeSpan);
    chip.appendChild(removeBtn);

    return chip;
  }

  private removeVariableChip(chip: HTMLElement): void {
    const parent = chip.parentNode;
    if (!parent) return;

    const editor = this.editorContentRef.nativeElement;

    const nextSibling = chip.nextSibling;
    if (nextSibling && nextSibling.nodeType === Node.TEXT_NODE && nextSibling.textContent === '\u00A0') {
      parent.removeChild(nextSibling);
    }

    parent.removeChild(chip);

    if (editor.innerText.trim() === '' && editor.getElementsByClassName('variable-chip').length === 0) {
      editor.innerHTML = '';
    }

    this.emitContentChange();
    this.updateUsedVariables();
    editor.focus();
  }

  public getRawContent(): string {
    return this.editorContentRef.nativeElement.innerHTML;
  }

  public getCleanContent(): string {
    const editorClone = this.editorContentRef.nativeElement.cloneNode(true) as HTMLElement;
    const chips = editorClone.querySelectorAll('.variable-chip');
    chips.forEach(chip => {
      const code = (chip as HTMLElement).dataset['variableCode'];
      if (code) {
        const textNode = document.createTextNode(`{{${code}}}`);
        chip.parentNode?.replaceChild(textNode, chip);
      }
    });
    return editorClone.innerHTML;
  }

  applyFormat(command: string, value?: string): void {
    document.execCommand(command, false, value);
    this.emitContentChange();
    this.editorContentRef.nativeElement.focus();
  }

  onFontSizeChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedFontSize = selectElement.value;
    this.applyFormat('fontSize', this.selectedFontSize);
  }

  onAlignmentChange(alignment: string): void {
    let command = '';
    switch (alignment) {
      case 'left': command = 'justifyLeft'; break;
      case 'center': command = 'justifyCenter'; break;
      case 'right': command = 'justifyRight'; break;
      case 'justify': command = 'justifyFull'; break;
    }
    this.selectedAlignment = alignment;
    this.applyFormat(command);
  }

  onInput(): void {
    this.emitContentChange();
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const text = event.clipboardData?.getData('text/plain');
    if (text) {
      document.execCommand('insertText', false, text);
    }
  }

  onKeyDown(event: KeyboardEvent): void {}

  private emitContentChange(): void {
    this.contentChange.emit(this.getRawContent());
  }

  private updateUsedVariables(): void {
    const foundVariables: VariableSelectOption[] = [];
    this.editorContentRef.nativeElement.querySelectorAll('.variable-chip').forEach(chip => {
      const code = (chip as HTMLElement).dataset['variableCode'];
      if (code) {
        const variable = this.availableVariables.find(v => v.code === code);
        if (variable && !foundVariables.some(fv => fv.publicId === variable.publicId)) {
          foundVariables.push(variable);
        }
      }
    });
    this.usedVariablesChange.emit(foundVariables);
  }

  private setupMutationObserver(): void {
    this.mutationObserver = new MutationObserver((mutations) => {
      let shouldUpdate = false;
      for (const mutation of mutations) {
        if (mutation.type === 'childList') {
          const added = Array.from(mutation.addedNodes);
          if (!added.every(node => (node as HTMLElement).classList?.contains('variable-chip'))) {
            shouldUpdate = true;
          }
        } else {
          shouldUpdate = true;
        }
      }
      if (shouldUpdate) {
        this.emitContentChange();
        this.updateUsedVariables();
      }
    });

    this.mutationObserver.observe(this.editorContentRef.nativeElement, {
      childList: true,
      subtree: true,
      characterData: true,
    });
  }
}
