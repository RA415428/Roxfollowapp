export type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'PARTIAL'
  | 'CANCELLED'
  | 'FAILED';

export function normalizeSmmStatus(rawStatus: string): OrderStatus {
  if (!rawStatus) return 'PENDING';
  
  const statusStr = rawStatus.toLowerCase().trim();

  if (statusStr.includes('pending')) return 'PENDING';
  if (statusStr.includes('processing')) return 'PROCESSING';
  if (statusStr.includes('in progress') || statusStr.includes('inprogress') || statusStr.includes('in_progress')) {
    return 'IN_PROGRESS';
  }
  if (statusStr.includes('completed') || statusStr.includes('success') || statusStr.includes('done')) {
    return 'COMPLETED';
  }
  if (statusStr.includes('partial')) return 'PARTIAL';
  if (statusStr.includes('cancelled') || statusStr.includes('canceled')) return 'CANCELLED';
  if (statusStr.includes('failed') || statusStr.includes('error')) return 'FAILED';

  return 'PROCESSING';
}

export async function fetchOrderStatus(apiUrl: string, apiKey: string, smmOrderId: string) {
  try {
    const response = await fetch(`${apiUrl}?key=${apiKey}&action=status&order=${smmOrderId}`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    return {
      status: normalizeSmmStatus(data.status || ''),
      remains: data.remains ?? null,
      charge: data.charge ?? null,
    };
  } catch (error) {
    console.error('Failed to fetch status from SMM provider:', error);
    return null;
  }
}
