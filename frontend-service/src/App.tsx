import { useState } from 'react';
import type { FormEvent } from 'react';
import axios from 'axios';

import type { CreditApplication, PredictionResult, FormErrors } from './types';
import { CopyableId } from './components/CopyableId';
import { styles } from './styles';

export default function App() {
  const [activeTab, setActiveTab] = useState<'evaluate' | 'history'>('evaluate');

  const [searchId, setSearchId] = useState<string>('');
  const [searchResult, setSearchResult] = useState<any | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [searchLoading, setSearchLoading] = useState<boolean>(false);

  const [formData, setFormData] = useState<CreditApplication>({
    RevolvingUtilizationOfUnsecuredLines: 0.15,
    age: 45,
    "NumberOfTime30-59DaysPastDueNotWorse": 0,
    "NumberOfTime60-89DaysPastDueNotWorse": 0,
    NumberOfTimes90DaysLate: 0,
    DebtRatio: 0.35,
    MonthlyIncome: 6000,
    NumberOfOpenCreditLinesAndLoans: 4,
    NumberRealEstateLoansOrLines: 1,
    NumberOfDependents: 1
  });

  const [result, setResult] = useState<PredictionResult | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
  const [systemError, setSystemError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const validateField = (name: keyof FormErrors, value: any) => {
    let error = '';
    if (value === null || value === undefined || value === '') {
      error = 'This field is required.';
      setFieldErrors(prev => ({ ...prev, [name]: error }));
      return error;
    }

    switch (name) {
      case 'age':
        if (value === 0) error = 'Age is required.';
        else if (value < 18) error = 'Minimum age is 18 years old.';
        else if (value > 120) error = 'Maximum age is 120 years old.';
        break;

      case 'MonthlyIncome':
        if (value < 0) error = 'Monthly income cannot be negative.';
        break;

      case 'NumberOfDependents':
        if (value < 0) error = 'Number of dependents cannot be negative.';
        break;

      case 'RevolvingUtilizationOfUnsecuredLines':
        if (value < 0) error = 'Utilization ratio cannot be negative.';
        break;

      case 'DebtRatio':
        if (value < 0) error = 'Debt ratio cannot be negative.';
        break;

      case 'NumberOfOpenCreditLinesAndLoans':
        if (value < 0) error = 'Number of open lines cannot be negative.';
        break;

      case 'NumberRealEstateLoansOrLines':
        if (value < 0) error = 'Number of real estate loans cannot be negative.';
        break;

      case 'NumberOfTime30-59DaysPastDueNotWorse':
      case 'NumberOfTime60-89DaysPastDueNotWorse':
      case 'NumberOfTimes90DaysLate':
        if (value < 0) error = 'Number of times past due cannot be negative.';
        break;

      default:
        break;
    }

    setFieldErrors(prev => ({ ...prev, [name]: error ? error : undefined }));
    return error;
  };

  const isFormInvalid = Object.values(fieldErrors).some(err => err !== undefined);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (isFormInvalid) return;
    setLoading(true);
    setResult(null);
    setSystemError(null);

    try {
      const response = await axios.post('http://localhost:8080/api/credit/applications/predict', formData);
      const { decision, probability, is_risky, modelVersion, credit_application_id } = response.data;

      setResult({
        decision,
        probability,
        isRisky: is_risky,
        modelVersion,
        creditApplicationId: credit_application_id
      });
    } catch (error) {
      setSystemError('Backend connection error. Please ensure Docker containers are running properly.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchHistory = async (e: FormEvent) => {
    e.preventDefault();
    if (!searchId.trim()) return;

    setSearchLoading(true);
    setSearchResult(null);
    setSearchError(null);

    try {
      const response = await axios.get(`http://localhost:8080/api/credit/applications/${searchId.trim()}`);
      setSearchResult(response.data);
    } catch (rawError: unknown) {
      const error = rawError as any;

      if (error.response) {
        const status = error.response.status;
        const errorMessage = error.response.data?.message || '';

        if (status === 404 || errorMessage.includes('Application not found')) {
          setSearchError(`Application with ID "${searchId}" not found in database.`);
        } else if (status === 502) {
          setSearchError('Integration error (502). The gateway server encountered an issue with downstream services.');
        } else {
          setSearchError(`Server error (${status}): Unable to process this request.`);
        }
      } else if (error.request) {
        setSearchError('Database connection error. Please ensure the backend server is running.');
      } else {
        setSearchError(`An unexpected error occurred: ${error.message}`);
      }
    } finally {
      setSearchLoading(false);
    }
  };

  return (
      <div style={styles.appWrapper}>
        <aside style={styles.sidebar}>
          <div style={styles.sidebarHeader}>
            <h2 style={styles.sidebarTitle}>ScoringApp</h2>
            <span style={styles.sidebarVersion}>v1.0 Core</span>
          </div>

          <div style={styles.menuGroup}>
            <button
                onClick={() => setActiveTab('evaluate')}
                style={{...styles.sidebarButton, ...(activeTab === 'evaluate' ? styles.sidebarButtonActive : {})}}
            >
              New Evaluation
            </button>
            <button
                onClick={() => setActiveTab('history')}
                style={{...styles.sidebarButton, ...(activeTab === 'history' ? styles.sidebarButtonActive : {})}}
            >
              Search Database
            </button>
          </div>

          <div style={styles.sidebarFooter}>
            <p style={{ margin: 0, fontWeight: 600, color: '#ffffff' }}>Tomasz Stępień</p>
            <p style={{ margin: '0.2rem 0 0 0', fontSize: '0.75rem', color: '#64748b' }}>Project Author</p>
          </div>
        </aside>

        <main style={styles.mainContent}>
          <div style={styles.contentContainer}>
            <header style={styles.header}>
              <h1 style={styles.title}>Credit Risk Assessment</h1>
              <p style={styles.subtitle}>Automated Scoring & Risk Evaluation Console</p>
            </header>

            {activeTab === 'evaluate' && (
                <div style={styles.viewLayout}>
                  <form onSubmit={handleSubmit} style={styles.form}>
                    <div style={{ ...styles.card, borderTop: '0.25rem solid #e2e8f0' }}>
                      <h3 style={styles.cardTitle}>Demographics & Income</h3>
                      <div style={styles.flexGrid}>
                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Borrower Age</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors.age ? styles.inputError : {})}}
                              value={formData.age || ''}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, age: val});
                                validateField('age', val);
                              }}
                          />
                          {fieldErrors.age && <span style={styles.errorMessage}>{fieldErrors.age}</span>}
                        </div>

                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Monthly Income ($)</label>
                          <input
                              type="number"
                              step="500"
                              style={{...styles.input, ...(fieldErrors.MonthlyIncome ? styles.inputError : {})}}
                              value={formData.MonthlyIncome ?? ''}
                              onChange={e => {
                                const val = e.target.value === '' ? null : Number(e.target.value);
                                setFormData({...formData, MonthlyIncome: val});
                                validateField('MonthlyIncome', val);
                              }}
                          />
                          {fieldErrors.MonthlyIncome && <span style={styles.errorMessage}>{fieldErrors.MonthlyIncome}</span>}
                        </div>

                        <div style={styles.flexInputGroupFull}>
                          <label style={styles.label}>Number of Dependents</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors.NumberOfDependents ? styles.inputError : {})}}
                              value={formData.NumberOfDependents ?? ''}
                              onChange={e => {
                                const val = e.target.value === '' ? null : Number(e.target.value);
                                setFormData({...formData, NumberOfDependents: val});
                                validateField('NumberOfDependents', val);
                              }}
                          />
                          {fieldErrors.NumberOfDependents && <span style={styles.errorMessage}>{fieldErrors.NumberOfDependents}</span>}
                        </div>
                      </div>
                    </div>

                    <div style={{ ...styles.card, borderTop: '0.25rem solid #e2e8f0' }}>
                      <h3 style={styles.cardTitle}>Financial Exposure</h3>
                      <div style={styles.flexGrid}>
                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Revolving Utilization</label>
                          <input
                              type="number"
                              step="0.01"
                              style={{...styles.input, ...(fieldErrors.RevolvingUtilizationOfUnsecuredLines ? styles.inputError : {})}}
                              value={formData.RevolvingUtilizationOfUnsecuredLines}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, RevolvingUtilizationOfUnsecuredLines: val});
                                validateField('RevolvingUtilizationOfUnsecuredLines', val);
                              }}
                          />
                          {fieldErrors.RevolvingUtilizationOfUnsecuredLines && <span style={styles.errorMessage}>{fieldErrors.RevolvingUtilizationOfUnsecuredLines}</span>}
                        </div>

                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Debt Ratio</label>
                          <input
                              type="number"
                              step="0.01"
                              style={{...styles.input, ...(fieldErrors.DebtRatio ? styles.inputError : {})}}
                              value={formData.DebtRatio}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, DebtRatio: val});
                                validateField('DebtRatio', val);
                              }}
                          />
                          {fieldErrors.DebtRatio && <span style={styles.errorMessage}>{fieldErrors.DebtRatio}</span>}
                        </div>

                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Open Credit Lines & Loans</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors.NumberOfOpenCreditLinesAndLoans ? styles.inputError : {})}}
                              value={formData.NumberOfOpenCreditLinesAndLoans}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, NumberOfOpenCreditLinesAndLoans: val});
                                validateField('NumberOfOpenCreditLinesAndLoans', val);
                              }}
                          />
                          {fieldErrors.NumberOfOpenCreditLinesAndLoans && <span style={styles.errorMessage}>{fieldErrors.NumberOfOpenCreditLinesAndLoans}</span>}
                        </div>

                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>Real Estate Loans</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors.NumberRealEstateLoansOrLines ? styles.inputError : {})}}
                              value={formData.NumberRealEstateLoansOrLines}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, NumberRealEstateLoansOrLines: val});
                                validateField('NumberRealEstateLoansOrLines', val);
                              }}
                          />
                          {fieldErrors.NumberRealEstateLoansOrLines && <span style={styles.errorMessage}>{fieldErrors.NumberRealEstateLoansOrLines}</span>}
                        </div>
                      </div>
                    </div>

                    <div style={{ ...styles.card, borderTop: '0.25rem solid #e2e8f0' }}>
                      <h3 style={styles.cardTitle}>Delinquency History</h3>
                      <div style={styles.flexGrid}>
                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>30-59 Days Past Due</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors["NumberOfTime30-59DaysPastDueNotWorse"] ? styles.inputError : {})}}
                              value={formData["NumberOfTime30-59DaysPastDueNotWorse"]}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, "NumberOfTime30-59DaysPastDueNotWorse": val});
                                validateField("NumberOfTime30-59DaysPastDueNotWorse", val);
                              }}
                          />
                          {fieldErrors["NumberOfTime30-59DaysPastDueNotWorse"] && <span style={styles.errorMessage}>{fieldErrors["NumberOfTime30-59DaysPastDueNotWorse"]}</span>}
                        </div>
                        <div style={styles.flexInputGroup}>
                          <label style={styles.label}>60-89 Days Past Due</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors["NumberOfTime60-89DaysPastDueNotWorse"] ? styles.inputError : {})}}
                              value={formData["NumberOfTime60-89DaysPastDueNotWorse"]}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, "NumberOfTime60-89DaysPastDueNotWorse": val});
                                validateField("NumberOfTime60-89DaysPastDueNotWorse", val);
                              }}
                          />
                          {fieldErrors["NumberOfTime60-89DaysPastDueNotWorse"] && <span style={styles.errorMessage}>{fieldErrors["NumberOfTime60-89DaysPastDueNotWorse"]}</span>}
                        </div>
                        <div style={styles.flexInputGroupFull}>
                          <label style={styles.label}>90+ Days Past Due</label>
                          <input
                              type="number"
                              style={{...styles.input, ...(fieldErrors.NumberOfTimes90DaysLate ? styles.inputError : {})}}
                              value={formData.NumberOfTimes90DaysLate}
                              onChange={e => {
                                const val = Number(e.target.value);
                                setFormData({...formData, NumberOfTimes90DaysLate: val});
                                validateField("NumberOfTimes90DaysLate", val);
                              }}
                          />
                          {fieldErrors.NumberOfTimes90DaysLate && <span style={styles.errorMessage}>{fieldErrors.NumberOfTimes90DaysLate}</span>}
                        </div>
                      </div>
                    </div>

                    <button type="submit" disabled={loading || isFormInvalid} style={styles.submitButton}>
                      {loading ? 'Processing Assessment...' : 'Run Credit Risk Evaluation →'}
                    </button>
                    <button
                        type="button"
                        onClick={() => {
                          setFormData({
                            RevolvingUtilizationOfUnsecuredLines: 0.15,
                            age: 45,
                            "NumberOfTime30-59DaysPastDueNotWorse": 0,
                            "NumberOfTime60-89DaysPastDueNotWorse": 0,
                            NumberOfTimes90DaysLate: 0,
                            DebtRatio: 0.35,
                            MonthlyIncome: 6000,
                            NumberOfOpenCreditLinesAndLoans: 4,
                            NumberRealEstateLoansOrLines: 1,
                            NumberOfDependents: 1
                          });
                          setFieldErrors({});
                          setSystemError(null);
                          setResult(null);
                        }}
                        style={{
                          marginTop: '1rem',
                          padding: '0.75rem',
                          color: '#64748b',
                          backgroundColor: 'transparent',
                          border: '0.05rem solid #cbd5e1',
                          borderRadius: '0.35rem',
                          cursor: 'pointer',
                          width: '100%',
                          fontWeight: 500
                        }}
                    >
                      Reset to Default Parameters
                    </button>
                  </form>

                  <div style={styles.resultContainer}>
                    {systemError && <div style={styles.errorCard}>{systemError}</div>}
                    {result ? (
                        <div style={{ ...styles.resultCard, borderTop: `0.35rem solid ${result.isRisky ? '#d9534f' : '#10b981'}` }}>
                          <h3 style={{ ...styles.resultTitle, color: result.isRisky ? '#d9534f' : '#10b981' }}>
                            {result.isRisky ? 'Application Rejected' : 'Application Approved'}
                          </h3>
                          <div style={styles.resultGrid}>
                            <p style={styles.dbText}><strong>Recommendation:</strong> {result.decision}</p>
                            <p style={styles.dbText}><strong>Probability of Default:</strong> {(result.probability * 100).toFixed(2)}%</p>
                            <p style={{ ...styles.dbText, marginTop: '0.6rem', borderTop: '0.05rem solid #e2e8f0', paddingTop: '0.6rem' }}>
                              <strong>Application ID:</strong> <br />
                              <CopyableId id={result.creditApplicationId} />
                            </p>
                          </div>
                        </div>
                    ) : (
                        <div style={styles.emptyResultPlaceholder}>
                          <p>Awaiting execution parameters...</p>
                          <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginTop: '0.25rem' }}>Fill the form and click execute to run ML model.</p>
                        </div>
                    )}
                  </div>
                </div>
            )}

            {activeTab === 'history' && (
                <div style={styles.historyLayout}>
                  <div style={styles.card}>
                    <h3 style={styles.cardTitle}>Find Application</h3>
                    <form onSubmit={handleSearchHistory} style={styles.searchForm}>
                      <input
                          type="text"
                          placeholder="Enter Application Business ID (e.g. APP-b53ddbdd)"
                          value={searchId}
                          onChange={e => setSearchId(e.target.value)}
                          style={styles.searchInput}
                      />
                      <button type="submit" disabled={searchLoading} style={styles.searchButton}>
                        {searchLoading ? 'Searching...' : 'Search'}
                      </button>
                    </form>
                  </div>

                  {searchError && <div style={styles.errorCard}>{searchError}</div>}

                  {searchResult && (
                      <div style={styles.card}>
                        <h3 style={{...styles.cardTitle, borderBottom: '0.05rem solid #e2e8f0', paddingBottom: '0.75rem', color: '#0f172a', textAlign: 'left'}}>
                          Profile Core Parameters: <CopyableId id={searchResult.credit_application_id} />
                        </h3>
                        <div style={{...styles.resultGrid, display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(18rem, 1fr))', gap: '1.25rem', marginTop: '1rem'}}>
                          <p style={styles.dbText}><strong>Age:</strong> {searchResult.age}</p>
                          <p style={styles.dbText}><strong>Monthly Income:</strong> {searchResult.MonthlyIncome !== null && searchResult.MonthlyIncome !== undefined ? `$${searchResult.MonthlyIncome}` : 'N/A'}</p>
                          <p style={styles.dbText}><strong>Debt Ratio:</strong> {searchResult.DebtRatio}</p>
                          <p style={styles.dbText}><strong>Revolving Utilization:</strong> {searchResult.RevolvingUtilizationOfUnsecuredLines}</p>
                          <p style={styles.dbText}><strong>Open Lines:</strong> {searchResult.NumberOfOpenCreditLinesAndLoans}</p>
                          <p style={styles.dbText}><strong>Real Estate Loans:</strong> {searchResult.NumberRealEstateLoansOrLines}</p>
                          <p style={styles.dbText}><strong>Number of Dependents:</strong> {searchResult.NumberOfDependents !== null && searchResult.NumberOfDependents !== undefined ? searchResult.NumberOfDependents : 'N/A'}</p>
                        </div>

                        <div style={{ marginTop: '2rem', borderTop: '0.1rem dashed #e2e8f0', paddingTop: '1.25rem' }}>
                          <h4 style={{ fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase', marginBottom: '1rem', textAlign: 'left' }}>
                             Historical Decisions
                          </h4>
                          {searchResult.decisions && searchResult.decisions.length > 0 ? (
                              searchResult.decisions.map((dec: any, i: number) => (
                                  <div key={i} style={{ ...styles.resultCard, margin: '0.6rem 0', borderLeft: `0.25rem solid ${dec.is_risky ? '#d9534f' : '#10b981'}`, backgroundColor: '#f8fafc', padding: '1.5rem' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 600 }}>
                                      <span style={{ color: dec.is_risky ? '#d9534f' : '#10b981' }}>{dec.decision}</span>
                                      <span style={{ color: '#334155' }}>Default Prob: {(dec.probability * 100).toFixed(2)}%</span>
                                    </div>
                                    <p style={{ fontSize: '0.8rem', color: '#64748b', margin: '0.5rem 0 0 0' }}>Timestamp: {dec.createdAt} | Version: {dec.modelVersion}</p>
                                  </div>
                              ))
                          ) : (
                              <p style={{ fontSize: '0.9rem', color: '#64748b', fontStyle: 'italic' }}>No historical decisions found for this profile record.</p>
                          )}
                        </div>
                      </div>
                  )}
                </div>
            )}
          </div>
        </main>
      </div>
  );
}