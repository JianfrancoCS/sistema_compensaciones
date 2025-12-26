import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Hiring } from './hiring';

describe('Hiring', () => {
  let component: Hiring;
  let fixture: ComponentFixture<Hiring>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Hiring]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Hiring);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
