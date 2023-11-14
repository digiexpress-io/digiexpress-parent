import {canContain} from '../../src/defaults';

describe('containmentRules', () => {
  it ('group can contain text', () => {
     expect(canContain('group', 'text')).to.equal(true);
  });

  it ('group can contain group', () => {
    expect(canContain('group', 'group')).to.equal(true);
 });

  it ('group can\'t contain survey', () => {
    expect(canContain('group', 'survey')).to.equal(false);
  });

  it ('text can\'t contain text', () => {
    expect(canContain('text', 'text')).to.equal(false);
  });

  it ('page can\'t contain text', () => {
    expect(canContain('page', 'text')).to.equal(false);
  });

  it ('page can\'t contain page', () => {
    expect(canContain('page', 'page')).to.equal(false);
  });

  it ('surveygroup can contain survey', () => {
    expect(canContain('surveygroup', 'survey')).to.equal(true);
  });

});
