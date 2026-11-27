import { FormControl, FormGroup } from '@angular/forms';
import { toIsoDate } from '../dates';
import {
  cpfValidator,
  isValidCpf,
  matchingFields,
  onlyDigits,
  pastDateValidator,
  phoneValidator,
} from './br-validators';

describe('br-validators', () => {
  it('strips non-digits', () => {
    expect(onlyDigits('123.456.789-09')).toBe('12345678909');
  });

  describe('CPF', () => {
    it('accepts valid numbers with or without mask', () => {
      expect(isValidCpf('529.982.247-25')).toBe(true);
      expect(isValidCpf('52998224725')).toBe(true);
    });

    it('rejects wrong check digits, wrong size and repeated digits', () => {
      expect(isValidCpf('529.982.247-24')).toBe(false);
      expect(isValidCpf('1234')).toBe(false);
      expect(isValidCpf('111.111.111-11')).toBe(false);
    });

    it('leaves emptiness to the required validator', () => {
      expect(cpfValidator(new FormControl(''))).toBeNull();
      expect(cpfValidator(new FormControl('123'))).toEqual({ cpf: true });
    });
  });

  describe('phone', () => {
    it('accepts 10 (landline) and 11 (mobile) digits', () => {
      expect(phoneValidator(new FormControl('(41) 3333-4444'))).toBeNull();
      expect(phoneValidator(new FormControl('(41) 99999-8888'))).toBeNull();
    });

    it('rejects other lengths', () => {
      expect(phoneValidator(new FormControl('9999-8888'))).toEqual({ phone: true });
    });
  });

  describe('matchingFields', () => {
    const group = (password: string, confirmation: string) =>
      new FormGroup({ password: new FormControl(password), confirm: new FormControl(confirmation) });

    it('flags different values', () => {
      expect(matchingFields('password', 'confirm')(group('a', 'b'))).toEqual({
        fieldsMismatch: true,
      });
    });

    it('passes equal values and an untouched confirmation', () => {
      expect(matchingFields('password', 'confirm')(group('a', 'a'))).toBeNull();
      expect(matchingFields('password', 'confirm')(group('a', ''))).toBeNull();
    });
  });

  describe('pastDate', () => {
    it('accepts yesterday, rejects today and the future', () => {
      const day = new Date();
      day.setDate(day.getDate() - 1);
      expect(pastDateValidator(new FormControl(toIsoDate(day)))).toBeNull();

      expect(pastDateValidator(new FormControl(toIsoDate(new Date())))).toEqual({ pastDate: true });
      day.setDate(day.getDate() + 10);
      expect(pastDateValidator(new FormControl(toIsoDate(day)))).toEqual({ pastDate: true });
    });
  });
});
