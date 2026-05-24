import { useState } from 'react';

export const CopyableId = ({ id }: { id: string }) => {
    const [copied, setCopied] = useState(false);
    const [isHovered, setIsHovered] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(id);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch (err) {
            console.error('Failed to copy text: ', err);
        }
    };

    return (
        <div style={{ position: 'relative', display: 'inline-block' }}>
      <span
          onClick={handleCopy}
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
          style={{
              fontSize: '0.95rem',
              color: '#2563eb',
              fontWeight: 600,
              fontFamily: 'monospace',
              cursor: 'pointer',
              backgroundColor: isHovered ? '#e2e8f0' : 'transparent',
              borderRadius: '0.25rem',
              padding: '0.2rem 0.5rem',
              transition: 'background-color 0.2s ease',
          }}
      >
        {id}
      </span>
            {isHovered && (
                <div style={{
                    position: 'absolute',
                    bottom: '125%',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    backgroundColor: '#1e293b',
                    color: '#ffffff',
                    padding: '0.35rem 0.6rem',
                    borderRadius: '0.25rem',
                    fontSize: '0.75rem',
                    whiteSpace: 'nowrap',
                    zIndex: 10,
                    boxShadow: '0 0.2rem 0.4rem rgba(0,0,0,0.15)',
                }}>
                    {copied ? 'Copied!' : 'Click to copy'}
                </div>
            )}
        </div>
    );
};