import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContractTemplates } from './contract-templates';

describe('ContractTemplates', () => {
  let component: ContractTemplates;
  let fixture: ComponentFixture<ContractTemplates>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContractTemplates]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContractTemplates);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
