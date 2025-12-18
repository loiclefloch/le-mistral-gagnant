import React from 'react';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  LineChart,
  Line,
} from 'recharts';

export const ScoringCharts = () => {
  // Données des scores par domaine
  const domainScores = [
    { name: '🔐 Sécurité', score: 20, weight: 18 },
    { name: '❌ Gestion d\'erreurs', score: 10, weight: 10 },
    { name: '🏷️ Versioning', score: 0, weight: 10 },
    { name: '🌐 URLs', score: 20, weight: 8 },
    { name: '🔧 Méthodes HTTP', score: 30, weight: 8 },
    { name: '📊 Codes de statut', score: 40, weight: 7 },
    { name: '📄 Pagination', score: 0, weight: 7 },
    { name: '⏱️ Rate Limiting', score: 0, weight: 6 },
    { name: '🔀 Négociation', score: 100, weight: 4 },
    { name: '💾 Caching', score: 0, weight: 5 },
    { name: '🔗 HATEOAS', score: 0, weight: 3 },
    { name: '📚 Documentation', score: 50, weight: 10 },
    { name: '👁️ Observabilité', score: 20, weight: 4 },
  ];

  // Données pour la progression
  const progressionData = [
    { phase: 'Actuel', score: 35 },
    { phase: 'Phase 1', score: 60 },
    { phase: 'Phase 2', score: 75 },
    { phase: 'Phase 3', score: 85 },
  ];

  // Données pour le radar (seulement les domaines principaux)
  const radarData = [
    { domain: 'Sécurité', value: 20 },
    { domain: 'Gestion erreurs', value: 10 },
    { domain: 'Versioning', value: 0 },
    { domain: 'URLs', value: 20 },
    { domain: 'Méthodes', value: 30 },
    { domain: 'Documentation', value: 50 },
  ];

  // Couleurs personnalisées
  const colors = {
    excellent: '#00c49f',
    good: '#0088fe',
    warning: '#ffbb28',
    critical: '#ff7c7c',
  };

  const getColor = (score) => {
    if (score >= 80) return colors.excellent;
    if (score >= 60) return colors.good;
    if (score >= 40) return colors.warning;
    return colors.critical;
  };

  return (
    <div style={{ width: '100%' }}>
      {/* Graphique 1: Barre des domaines */}
      <div style={{ marginBottom: '40px' }}>
        <h3>📊 Scores par Domaine</h3>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart data={domainScores}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
            <YAxis domain={[0, 100]} />
            <Tooltip formatter={(value) => `${value}/100`} />
            <Bar dataKey="score" fill="#8884d8" name="Score" radius={[8, 8, 0, 0]}>
              {domainScores.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={getColor(entry.score)} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Graphique 2: Progression */}
      <div style={{ marginBottom: '40px' }}>
        <h3>📈 Progression Prévue</h3>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={progressionData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="phase" />
            <YAxis domain={[0, 100]} />
            <Tooltip formatter={(value) => `${value}/100`} />
            <Legend />
            <Line
              type="monotone"
              dataKey="score"
              stroke="#8884d8"
              dot={{ fill: '#8884d8', r: 6 }}
              activeDot={{ r: 8 }}
              name="Score Global"
              strokeWidth={2}
            />
          </LineChart>
        </ResponsiveContainer>
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <p style={{ fontSize: '18px', fontWeight: 'bold', color: '#00c49f' }}>
            +50 points en 3-4 semaines 🚀
          </p>
        </div>
      </div>

      {/* Graphique 3: Radar */}
      <div style={{ marginBottom: '40px' }}>
        <h3>🎯 Analyse Multi-Domaines</h3>
        <ResponsiveContainer width="100%" height={400}>
          <RadarChart data={radarData}>
            <PolarGrid />
            <PolarAngleAxis dataKey="domain" />
            <PolarRadiusAxis domain={[0, 100]} />
            <Radar
              name="Score"
              dataKey="value"
              stroke="#8884d8"
              fill="#8884d8"
              fillOpacity={0.6}
            />
            <Tooltip formatter={(value) => `${value}/100`} />
          </RadarChart>
        </ResponsiveContainer>
      </div>

      {/* Graphique 4: Pie chart status */}
      <div style={{ marginBottom: '40px' }}>
        <h3>🔴 État des Domaines</h3>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={[
                { name: 'Critique (0-39)', value: 9 },
                { name: 'Mauvais (40-59)', value: 2 },
                { name: 'Bon (60-79)', value: 1 },
                { name: 'Excellent (80+)', value: 1 },
              ]}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, value }) => `${name}: ${value}`}
              outerRadius={100}
            >
              <Cell fill="#ff7c7c" />
              <Cell fill="#ffbb28" />
              <Cell fill="#0088fe" />
              <Cell fill="#00c49f" />
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* Résumé statistique */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '20px',
        marginTop: '40px',
      }}>
        <div style={{
          padding: '20px',
          backgroundColor: '#fff3cd',
          borderRadius: '8px',
          borderLeft: '4px solid #ffc107',
        }}>
          <h4>⚠️ Domaines Critiques</h4>
          <p style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#ff7c7c' }}>9</p>
          <p style={{ margin: '10px 0 0 0', fontSize: '12px', color: '#666' }}>
            Score 0-39
          </p>
        </div>
        <div style={{
          padding: '20px',
          backgroundColor: '#e7f3ff',
          borderRadius: '8px',
          borderLeft: '4px solid #0088fe',
        }}>
          <h4>📊 Score Global</h4>
          <p style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#0088fe' }}>35/100</p>
          <p style={{ margin: '10px 0 0 0', fontSize: '12px', color: '#666' }}>
            Niveau: CRITIQUE
          </p>
        </div>
        <div style={{
          padding: '20px',
          backgroundColor: '#d4edda',
          borderRadius: '8px',
          borderLeft: '4px solid #00c49f',
        }}>
          <h4>✅ Points Positifs</h4>
          <p style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#00c49f' }}>1</p>
          <p style={{ margin: '10px 0 0 0', fontSize: '12px', color: '#666' }}>
            Négociation de contenu (100/100)
          </p>
        </div>
        <div style={{
          padding: '20px',
          backgroundColor: '#e2e3e5',
          borderRadius: '8px',
          borderLeft: '4px solid #6c757d',
        }}>
          <h4>🚀 Amélioration Prévue</h4>
          <p style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#6c757d' }}>+50</p>
          <p style={{ margin: '10px 0 0 0', fontSize: '12px', color: '#666' }}>
            Points (3-4 semaines)
          </p>
        </div>
      </div>
    </div>
  );
};

